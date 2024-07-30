package com.zqswjtu.freemall.search.service;

import com.zqswjtu.freemall.search.vo.SearchParamVo;
import com.zqswjtu.freemall.search.vo.SearchResultVo;

public interface FreemallSearchService {
    SearchResultVo search(SearchParamVo paramVo);
}
