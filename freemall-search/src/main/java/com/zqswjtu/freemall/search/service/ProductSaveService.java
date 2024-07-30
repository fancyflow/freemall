package com.zqswjtu.freemall.search.service;


import com.zqswjtu.common.to.es.SkuEsModel;

import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels);

}
