package com.zqswjtu.freemall.search;

import com.alibaba.fastjson.JSON;
import com.zqswjtu.freemall.search.config.FreemallElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class FreemallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Data
    private static class User {
        private String name;
        private Integer age;
        private Character gender;
    }

    /**
     * 测试存储数据到es
     */
    @Test
    void contextLoads() {
        System.out.println(client);
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        // 1、第一种方式
        // indexRequest.source("username", "zhangsan", "age", 18, "gender", 'M');
        // 2、常用方式
        User user = new User();
        user.setName("aaa");
        user.setGender('M');
        user.setAge(18);
        String json = JSON.toJSONString(user);
        indexRequest.source(json, XContentType.JSON);

        try {
            // 执行操作
            IndexResponse index = client.index(indexRequest, FreemallElasticSearchConfig.COMMON_OPTIONS);

            // 执行完操作后的后续处理
            System.out.println(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
