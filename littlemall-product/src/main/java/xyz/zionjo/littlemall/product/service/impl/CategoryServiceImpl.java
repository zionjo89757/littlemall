package xyz.zionjo.littlemall.product.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.product.dao.CategoryDao;
import xyz.zionjo.littlemall.product.entity.CategoryEntity;
import xyz.zionjo.littlemall.product.service.CategoryBrandRelationService;
import xyz.zionjo.littlemall.product.service.CategoryService;
import xyz.zionjo.littlemall.product.vo.Catalog2Vo;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        //获取全部分类信息
        List<CategoryEntity> entities = baseMapper.selectList(null);


        //组装成父子结构
        List<CategoryEntity> lv1Menus = entities.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                .map(menu -> {
                    menu.setChildren(getChildren(menu, entities));
                    return menu;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return lv1Menus;
    }

    /**
     * 批量删除子菜单
     *
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被其他的地方引用

        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        findParentPath(catelogId, paths);

        Collections.reverse(paths);


        return paths.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category",key ="'level1'" ),
//            @CacheEvict(value = "category",key ="'catalogJson'" )
//    })
    @CacheEvict(value = "category",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 查询所有一级分类
     *
     * @return
     */
    @Cacheable(value = {" category"}, key="'level1'")
    @Override
    public List<CategoryEntity> getLevel1Category() {
        return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
    }

    @Cacheable(value = "category",key="'catalogJson'",sync = true)
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        // 从数据库查出所有分类
        List<CategoryEntity> categoryAll = this.baseMapper.selectList(null);
        List<CategoryEntity> level1Category = getParent_cid(categoryAll, 0L);

        Map<String, List<Catalog2Vo>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2Category = getParent_cid(categoryAll, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Category != null && level1Category.size() > 0) {
                catalog2Vos = level2Category.stream().map(item -> {

                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());

                    List<CategoryEntity> level3Category = getParent_cid(categoryAll, item.getCatId());
                    if (level3Category != null && level3Category.size() > 0) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Category.stream().map(level3 -> {
                            return new Catalog2Vo.Catalog3Vo(item.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return map;
    }

    // TODO 产生堆外内存溢出：OutOfDirectMemoryError
    // lettuce的bug导致netty堆外内存溢出 -Dio.netty.maxDirectMemory
    // 升级lettuce客户端、切换使用jedis
    public Map<String, List<Catalog2Vo>> getCatalogJson2() {

        /** TODO 缓存常见问题
         * 1 空结果缓存：缓存穿透
         * 2 设置过期时间（加随机值）：解决缓存穿透
         * 3 加锁：解决缓存击穿
         */
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();

            return catalogJsonFromDb;
        }
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });


        return result;
    }

    /**
     * 查询数据库
     * TODO 考虑分布式锁情况
     *
     * @return
     */
    public synchronized Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        return getCatalogJsonFromDb();
    }

    private Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return result;
        }

        // 从数据库查出所有分类
        List<CategoryEntity> categoryAll = this.baseMapper.selectList(null);
        List<CategoryEntity> level1Category = getParent_cid(categoryAll, 0L);

        Map<String, List<Catalog2Vo>> map = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> level2Category = getParent_cid(categoryAll, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Category != null && level1Category.size() > 0) {
                catalog2Vos = level2Category.stream().map(item -> {

                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, item.getCatId().toString(), item.getName());

                    List<CategoryEntity> level3Category = getParent_cid(categoryAll, item.getCatId());
                    if (level3Category != null && level3Category.size() > 0) {
                        List<Catalog2Vo.Catalog3Vo> catalog3Vos = level3Category.stream().map(level3 -> {
                            return new Catalog2Vo.Catalog3Vo(item.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(catalog3Vos);
                    }

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        String s = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);

        return map;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        // 占分布式锁。去redis占坑
        RLock lock = redisson.getLock("catalogJson-lock");

        lock.lock();
        Map<String, List<Catalog2Vo>> catalogJsonFromDb = null;
        try {
            catalogJsonFromDb = getCatalogJsonFromDb();
        } finally {
            lock.unlock();
        }

        return catalogJsonFromDb;


    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            //stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = null;
            try {

                catalogJsonFromDb = getCatalogJsonFromDb();
            } finally {
                //使用lua简本脚本解锁，保证原子性
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then  return redis.call('del',KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)){
//                stringRedisTemplate.delete("lock");
//            }

            return catalogJsonFromDb;


        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithLocalLock();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryAll, Long parentCid) {
        return categoryAll.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        //return this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (!byId.getParentCid().equals(0L)) {
            findParentPath(byId.getParentCid(), paths);
        }
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                .filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
                .map(categoryEntity -> {
                    if (categoryEntity.getCatLevel() < 3) categoryEntity.setChildren(getChildren(categoryEntity, all));
                    return categoryEntity;
                })
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return children;
    }


}