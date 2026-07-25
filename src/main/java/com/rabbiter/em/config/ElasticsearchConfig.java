package com.rabbiter.em.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置类
 * 配置Elasticsearch客户端连接和相关Bean
 * 使用7.12.1版本兼容配置
 *
 * 通过 app.es.enabled 控制是否启用，关闭后不创建任何 ES Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "app.es", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.cluster-nodes}")
    private String clusterNodes;

    @Value("${spring.elasticsearch.cluster-name}")
    private String clusterName;

    /**
     * 创建RestHighLevelClient Bean（兼容7.12.1版本）
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] nodes = clusterNodes.split(",");
        HttpHost[] httpHosts = new HttpHost[nodes.length];
        
        for (int i = 0; i < nodes.length; i++) {
            String[] nodeInfo = nodes[i].trim().split(":");
            String host = nodeInfo[0];
            int port = Integer.parseInt(nodeInfo[1]);
            httpHosts[i] = new HttpHost(host, port, "http");
        }
        
        return new RestHighLevelClient(
                RestClient.builder(httpHosts)
                        .setRequestConfigCallback(requestConfigBuilder -> 
                            requestConfigBuilder
                                .setConnectTimeout(10000)
                                .setSocketTimeout(30000)
                        )
        );
    }
}