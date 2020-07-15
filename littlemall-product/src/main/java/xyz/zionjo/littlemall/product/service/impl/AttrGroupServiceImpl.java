package xyz.zionjo.littlemall.product.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.common.utils.Query;

import xyz.zionjo.littlemall.product.dao.AttrGroupDao;
import xyz.zionjo.littlemall.product.entity.AttrEntity;
import xyz.zionjo.littlemall.product.entity.AttrGroupEntity;
import xyz.zionjo.littlemall.product.service.AttrGroupService;
import xyz.zionjo.littlemall.product.service.AttrService;
import xyz.zionjo.littlemall.product.vo.AttrGroupWithAttrsVo;
import xyz.zionjo.littlemall.product.vo.SkuItemVo;
import xyz.zionjo.littlemall.product.vo.SpuItemAttrGroupVo;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if(!StringUtils.isEmpty(key)){
            wrapper.and(obj->{
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }

        if(catelogId == 0){
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );

            return new PageUtils(page);
        }else{
            wrapper.eq("catelog_id",catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id查出所有分组以及这些分组里面的属性
     * @param catelogId
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 1 查询分组信息
        List<AttrGroupEntity> entities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        // 2 查询所有属性信息
        List<AttrGroupWithAttrsVo> collect = entities.stream().map(item -> {
            AttrGroupWithAttrsVo attrsvo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item,attrsvo);
            List<AttrEntity> attr = attrService.getRelationAttr(attrsvo.getAttrGroupId());
            attrsvo.setAttrs(attr);
            return attrsvo;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * 查出当前spu对应的所有属性的分组信息以及当前分组下所有属性对应的值
     * @param spuId
     * @param catalogId
     * @return
     */
    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        List<SpuItemAttrGroupVo> vos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }

}