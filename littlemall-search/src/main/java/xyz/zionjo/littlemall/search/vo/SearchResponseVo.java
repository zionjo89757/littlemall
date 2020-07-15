package xyz.zionjo.littlemall.search.vo;

import lombok.Data;
import xyz.zionjo.common.to.es.SkuEsModel;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResponseVo {
    private List<SkuEsModel> products;

    private Integer pageNum;
    private Long total;
    private Integer totalPages;
    private List<Integer> pageNavs;

    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;

    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();


    @Data
    public static class NavVo{
        private  String  NavName;
        private String NavValue;
        private String link;
    }



    @Data
    public static class BrandVo{
        private  Long  brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private  Long  catalogId;
        private String catalogName;
    }
}
