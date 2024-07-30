package com.zqswjtu.freemall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zqswjtu.common.to.es.SkuEsModel;
import com.zqswjtu.freemall.search.config.FreemallElasticSearchConfig;
import com.zqswjtu.freemall.search.constant.EsConstant;
import com.zqswjtu.freemall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service("productSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) {
        // 将数据保存到es
        // 1、给es中建立索引 product 并且需要建立好映射关系
        // 2、给es中保存数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = null;
        try {
            bulk = restHighLevelClient.bulk(bulkRequest, FreemallElasticSearchConfig.COMMON_OPTIONS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // TODO 处理错误
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);
        return bulk.hasFailures();
    }

}
