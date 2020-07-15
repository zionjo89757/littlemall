package xyz.zionjo.littlemall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import xyz.zionjo.littlemall.product.entity.AttrEntity;

import java.util.List;
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;
    /**
     * 分类所属分组完整信息
     */
    private List<AttrEntity> attrs;
}
