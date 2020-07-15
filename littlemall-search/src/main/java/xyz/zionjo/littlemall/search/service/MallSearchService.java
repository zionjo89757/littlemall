package xyz.zionjo.littlemall.search.service;

import xyz.zionjo.littlemall.search.vo.SearchParamVo;
import xyz.zionjo.littlemall.search.vo.SearchResponseVo;

public interface MallSearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);
}
