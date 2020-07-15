package xyz.zionjo.littlemall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.zionjo.common.to.es.SkuEsModel;
import xyz.zionjo.common.utils.R;
import xyz.zionjo.littlemall.search.config.ESConfig;
import xyz.zionjo.littlemall.search.constant.ESConstant;
import xyz.zionjo.littlemall.search.feign.ProductFeignService;
import xyz.zionjo.littlemall.search.service.MallSearchService;
import xyz.zionjo.littlemall.search.vo.AttrResponseVo;
import xyz.zionjo.littlemall.search.vo.BrandVo;
import xyz.zionjo.littlemall.search.vo.SearchParamVo;
import xyz.zionjo.littlemall.search.vo.SearchResponseVo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    /**
     *
     * @param searchParamVo 所有检索参数
     * @return 检索结果
     */
    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {
        // TODO 动态构建出查询所需要的DSL语句
        SearchResponseVo searchResponseVo = null;
        // 1. 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParamVo);
        try {
            // 2. 执行检索请求
            SearchResponse response = client.search(searchRequest, ESConfig.COMMON_OPTIONS);

            // 3.分析响应数据包装成需要的格式
            searchResponseVo = buildSearchResult(response,searchParamVo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponseVo;

    }

    /**
     * 准备检索请求
     * 模糊匹配、过滤（按照属性、分类、品牌、价格区间、库存），排序、分页、高亮、聚合分析
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParamVo param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // query查询、过滤（按照属性、分类、品牌、价格区间、库存）
        // 1 bool
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 must-模糊匹配
        if(!StringUtils.isEmpty(param.getKeyword())){
            boolQuery.must(QueryBuilders.matchQuery("skuTitle",param.getKeyword()));
        }
        // 1.2.1 filter-分类
        if(param.getCatalog3Id() != null){
            boolQuery.filter(QueryBuilders.termQuery("catalogId",param.getCatalog3Id()));
        }
        // 1.2.2 filter-品牌
        if(param.getBrandId()!= null && param.getBrandId().size() > 0){
            boolQuery.filter(QueryBuilders.termsQuery("brandId",param.getBrandId()));

        }
        // 1.2.3 filter-库存
        if(param.getHasStock() != null){

            boolQuery.filter(QueryBuilders.termQuery("hasStock",param.getHasStock() == 1));
        }

        // 1.2.4 filter-价格区间
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2){
                rangeQuery.gte(s[0]).lte(s[1]);
            }else if(s.length == 1){
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(s[0]);
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);

        }
        // 1.2.5 filter-属性
        if(param.getAttrs() != null && param.getAttrs().size()>0){

            for(String attrStr : param.getAttrs()){
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue",attrValues));
                // 每一个都需要生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }

        sourceBuilder.query(boolQuery);

        // 排序、分页、高亮

        // 2.1 排序
        if(!StringUtils.isEmpty(param.getSort())){
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc")?SortOrder.ASC:SortOrder.DESC;
            sourceBuilder.sort(s[0],order);
        }
        // 2.2 分页
        sourceBuilder.from((param.getPageNum()-1)*ESConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(ESConstant.PRODUCT_PAGESIZE);

        //2.3 高亮
        if(!StringUtils.isEmpty(param.getKeyword())){
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("tkuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }


        // 聚合分析
        // 3.1 品牌聚合

        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId").size(50);
        // 3.1.1 品牌聚合的子聚合
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));

        sourceBuilder.aggregation(brand_agg);

        // 3.2 分类聚合
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId").size(20);
        // 3.2.1 分类聚合的子聚合
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        sourceBuilder.aggregation(catalog_agg);

        // 3.3 属性聚合
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg","attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));

        attr_agg.subAggregation(attr_id_agg);

        sourceBuilder.aggregation(attr_agg);

        String s = sourceBuilder.toString();
        System.out.println("DSL"+s);

        return new SearchRequest(new String[]{ESConstant.PRODUCT_INDEX}, sourceBuilder);

    }

    /**
     * 构建结果数据
     * @param response
     * @param param
     * @return
     */
    private SearchResponseVo buildSearchResult(SearchResponse response, SearchParamVo param) {
        SearchResponseVo result = new SearchResponseVo();
        SearchHits hits = response.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        // 1.返回查询到的所有商品
        if(hits.getHits() != null && hits.getHits().length > 0){
            for (SearchHit hit : hits.getHits()) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString,SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String highlight = skuTitle.getFragments()[0].string();
                    esModel.setSkuTitle(highlight);
                }
                esModels.add(esModel);
            }

        }
        result.setProducts(esModels);

        // 2.当前所有商品涉及到的所有属性信息
        List<SearchResponseVo.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResponseVo.AttrVo attrVo = new SearchResponseVo.AttrVo();
            // 属性id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 属性名

            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 所有属性值
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);



            attrVos.add(attrVo);

        }

        result.setAttrs(attrVos);

        // 3.当前所有商品涉及到的所有品牌信息
        List<SearchResponseVo.BrandVo> brandVos = new ArrayList<>();
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResponseVo.BrandVo brandVo = new SearchResponseVo.BrandVo();
            // 品牌id
            String keyAsString = bucket.getKeyAsString();
            brandVo.setBrandId(Long.valueOf(keyAsString));
            // 品牌名
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);
            // 品牌图片
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 4.当前所有商品涉及到的所有品牌信息
        List<SearchResponseVo.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResponseVo.CatalogVo catalogVo = new SearchResponseVo.CatalogVo();
            // 分类id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.valueOf(keyAsString));
            // 分类名
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // 5.分类信息
        // 5.1 页码
        result.setPageNum(param.getPageNum());

        long total = hits.getTotalHits().value;
        // 5.2 总记录数
        result.setTotal(total);

        int totalPages = (int)total%ESConstant.PRODUCT_PAGESIZE == 0? (int)total/ESConstant.PRODUCT_PAGESIZE : ((int)total/ESConstant.PRODUCT_PAGESIZE+1);
        // 5.3 总页码
        result.setTotalPages(totalPages);
        List<Integer> pageNavs =new ArrayList<>();
        for(int i=1;i<=totalPages;i++){
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        // 6 构建面包屑导航功能
        if(param.getAttrs() != null && param.getAttrs().size() > 0){

            List<SearchResponseVo.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResponseVo.NavVo navVo = new SearchResponseVo.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                result.getAttrIds().add(Long.parseLong(s[0]));
                // TODO 远程调用商品服务
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName("--");
                }
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.littlemall.com/list.html?"+ replace);
                return navVo;
            }).collect(Collectors.toList());


            result.setNavs(collect);
        }

        if(param.getBrandId()!= null && param.getBrandId().size()>0){
            List<SearchResponseVo.NavVo> navs = result.getNavs();
            SearchResponseVo.NavVo navVo = new SearchResponseVo.NavVo();
            navVo.setNavName("品牌");
            // TODO 远程调用商品服务
            R r = productFeignService.brandInfos(param.getBrandId());
            String replace = "";
            if(r.getCode() == 0){
                List<BrandVo> brands = r.getData("brands", new TypeReference<List<BrandVo>>() {});
                StringBuffer buffer = new StringBuffer();
                for(BrandVo brandVo : brands){
                    buffer.append(brandVo.getBrandName()+";");
                    replace = replaceQueryString(param,""+brandVo.getBrandId(),"brandId");
                }
                navVo.setNavValue(buffer.toString());
            }else{
                navVo.setNavName("--");
            }
            navVo.setLink("http://search.littlemall.com/list.html?"+ replace);
            navs.add(navVo);
        }

        return result;
    }

    private String replaceQueryString(SearchParamVo param, String attr,String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            encode = encode.replace("+", "%20").replace("%28","(").replace("%29",")");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String replace = param.get_queryString().replace("&"+key+"=" + encode, "");
        replace = replace.replace(key+"=" + encode, "");
        return replace;
    }


}
