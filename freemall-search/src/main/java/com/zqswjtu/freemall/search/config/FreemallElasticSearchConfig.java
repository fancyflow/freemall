package com.zqswjtu.freemall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FreemallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS = RequestOptions.DEFAULT.toBuilder().build();

    @Bean
    public RestHighLevelClient esRestClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost(
                "192.168.56.10", 9200, "http"
        )));
    }
}
