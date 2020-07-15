package xyz.zionjo.littlemall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.zionjo.common.utils.PageUtils;
import xyz.zionjo.littlemall.product.entity.CategoryEntity;
import xyz.zionjo.littlemall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zionjo9
 * @email 1227597575@qq.com
 * @date 2020-05-09 10:57:14
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getLevel1Category();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

