package xyz.zionjo.littlemall.search.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import xyz.zionjo.littlemall.search.service.MallSearchService;
import xyz.zionjo.littlemall.search.vo.SearchParamVo;
import xyz.zionjo.littlemall.search.vo.SearchResponseVo;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVo searchParamVo, Model model, HttpServletRequest request){
        searchParamVo.set_queryString(request.getQueryString());
        SearchResponseVo result = mallSearchService.search(searchParamVo);
        model.addAttribute("result",result);
        return "list";
    }
}
