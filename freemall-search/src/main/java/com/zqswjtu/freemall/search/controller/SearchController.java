package com.zqswjtu.freemall.search.controller;

import com.zqswjtu.freemall.search.service.FreemallSearchService;
import com.zqswjtu.freemall.search.vo.SearchParamVo;
import com.zqswjtu.freemall.search.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    FreemallSearchService freemallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParamVo paramVo, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        paramVo.setQueryString(queryString);
        SearchResultVo result = freemallSearchService.search(paramVo);
        model.addAttribute("result", result);
        return "list";
    }
}
