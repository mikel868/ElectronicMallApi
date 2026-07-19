package com.rabbiter.em.service;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.dto.GoodDocument;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 服务类
 * 提供商品搜索和数据同步功能
 * 使用RestHighLevelClient直接操作Elasticsearch 7.12.1
 */
@Service
public class ElasticsearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final String INDEX_NAME = "goods";

    /**
     * 创建索引
     * @return 创建结果
     */
    public boolean createIndex() {
        try {
            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查索引是否存在
     * @return 索引是否存在
     */
    public boolean indexExists() {
        try {
            GetIndexRequest request = new GetIndexRequest();
            request.indices(INDEX_NAME);
            return restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除索引
     * @return 删除结果
     */
    public boolean deleteIndex() {
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(INDEX_NAME);
            return restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT).isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 保存商品文档到Elasticsearch
     * @param goodDocument 商品文档
     * @return 保存结果
     */
    public boolean saveGoodDocument(GoodDocument goodDocument) {
        try {
            IndexRequest request = new IndexRequest(INDEX_NAME);
            request.id(goodDocument.getId().toString());
            request.source(convertToJson(goodDocument), XContentType.JSON);
            
            IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            return response.getResult().equals(org.elasticsearch.action.DocWriteResponse.Result.CREATED) ||
                   response.getResult().equals(org.elasticsearch.action.DocWriteResponse.Result.UPDATED);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据ID查找商品文档
     * @param id 商品ID
     * @return 商品文档
     */
    public GoodDocument findGoodDocumentById(Long id) {
        try {
            GetRequest request = new GetRequest(INDEX_NAME, id.toString());
            GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
            
            if (response.isExists()) {
                Map<String, Object> source = response.getSourceAsMap();
                return convertFromMap(source);
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 删除商品文档
     * @param id 商品ID
     * @return 删除结果
     */
    public boolean deleteGoodDocument(Long id) {
        try {
            DeleteRequest request = new DeleteRequest(INDEX_NAME, id.toString());
            DeleteResponse response = restHighLevelClient.delete(request, RequestOptions.DEFAULT);
            return response.getResult().equals(org.elasticsearch.action.DocWriteResponse.Result.DELETED);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据关键词搜索商品
     * @param keyword 搜索关键词
     * @param from 起始位置
     * @param size 每页大小
     * @return 搜索结果
     */
    public List<GoodDocument> searchGoods(String keyword, int from, int size) {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.multiMatchQuery(keyword, "name", "description"));
            sourceBuilder.from(from);
            sourceBuilder.size(size);
            sourceBuilder.sort("sales", org.elasticsearch.search.sort.SortOrder.DESC);
            
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            
            List<GoodDocument> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                results.add(convertFromMap(source));
            }
            
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 根据商品名称搜索
     * @param name 商品名称
     * @param from 起始位置
     * @param size 每页大小
     * @return 搜索结果
     */
    public List<GoodDocument> searchGoodsByName(String name, int from, int size) {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.matchQuery("name", name));
            sourceBuilder.from(from);
            sourceBuilder.size(size);
            sourceBuilder.sort("sales", org.elasticsearch.search.sort.SortOrder.DESC);
            
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            
            List<GoodDocument> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                results.add(convertFromMap(source));
            }
            
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 根据分类ID搜索商品
     * @param categoryId 分类ID
     * @param from 起始位置
     * @param size 每页大小
     * @return 搜索结果
     */
    public List<GoodDocument> searchGoodsByCategory(Long categoryId, int from, int size) {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.termQuery("categoryId", categoryId));
            sourceBuilder.from(from);
            sourceBuilder.size(size);
            sourceBuilder.sort("sales", org.elasticsearch.search.sort.SortOrder.DESC);
            
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            
            List<GoodDocument> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                results.add(convertFromMap(source));
            }
            
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 搜索推荐商品
     * @param from 起始位置
     * @param size 每页大小
     * @return 搜索结果
     */
    public List<GoodDocument> searchRecommendedGoods(int from, int size) {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            
            sourceBuilder.query(QueryBuilders.termQuery("recommend", true));
            sourceBuilder.from(from);
            sourceBuilder.size(size);
            sourceBuilder.sort("sales", org.elasticsearch.search.sort.SortOrder.DESC);
            
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            
            List<GoodDocument> results = new ArrayList<>();
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> source = hit.getSourceAsMap();
                results.add(convertFromMap(source));
            }
            
            return results;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 将Good实体转换为GoodDocument
     * @param good 商品实体
     * @return 商品文档
     */
    public GoodDocument convertToDocument(Good good) {
        GoodDocument document = new GoodDocument();
        document.setId(good.getId());
        document.setName(good.getName());
        document.setDescription(good.getDescription());
        document.setDiscount(good.getDiscount());
        document.setSales(good.getSales());
        document.setSaleMoney(good.getSaleMoney());
        document.setCategoryId(good.getCategoryId());
        document.setImgs(good.getImgs());
        document.setRecommend(good.getRecommend());
        document.setCreateTime(good.getCreateTime());
        return document;
    }

    /**
     * 将GoodDocument转换为JSON字符串
     * @param document 商品文档
     * @return JSON字符串
     */
    private String convertToJson(GoodDocument document) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"description\":\"%s\",\"discount\":%f,\"sales\":%d,\"saleMoney\":\"%s\",\"categoryId\":%d,\"imgs\":\"%s\",\"recommend\":%s,\"createTime\":\"%s\"}",
            document.getId(),
            document.getName() != null ? document.getName().replace("\"", "\\\"") : "",
            document.getDescription() != null ? document.getDescription().replace("\"", "\\\"") : "",
            document.getDiscount() != null ? document.getDiscount() : 0.0,
            document.getSales() != null ? document.getSales() : 0,
            document.getSaleMoney() != null ? document.getSaleMoney().toString() : "0",
            document.getCategoryId() != null ? document.getCategoryId() : 0,
            document.getImgs() != null ? document.getImgs().replace("\"", "\\\"") : "",
            document.getRecommend() != null ? document.getRecommend() : false,
            document.getCreateTime() != null ? document.getCreateTime() : ""
        );
    }

    /**
     * 从Map转换为GoodDocument
     * @param source Map数据
     * @return 商品文档
     */
    private GoodDocument convertFromMap(Map<String, Object> source) {
        GoodDocument document = new GoodDocument();
        document.setId(Long.valueOf(source.get("id").toString()));
        document.setName(source.get("name") != null ? source.get("name").toString() : "");
        document.setDescription(source.get("description") != null ? source.get("description").toString() : "");
        document.setDiscount(source.get("discount") != null ? Double.valueOf(source.get("discount").toString()) : 0.0);
        document.setSales(source.get("sales") != null ? Integer.valueOf(source.get("sales").toString()) : 0);
        document.setSaleMoney(source.get("saleMoney") != null ? new java.math.BigDecimal(source.get("saleMoney").toString()) : java.math.BigDecimal.ZERO);
        document.setCategoryId(source.get("categoryId") != null ? Long.valueOf(source.get("categoryId").toString()) : 0L);
        document.setImgs(source.get("imgs") != null ? source.get("imgs").toString() : "");
        document.setRecommend(source.get("recommend") != null ? Boolean.valueOf(source.get("recommend").toString()) : false);
        document.setCreateTime(source.get("createTime") != null ? source.get("createTime").toString() : "");
        return document;
    }

    /**
     * 获取索引中的文档总数
     * @return 文档总数
     */
    public long getDocumentCount() {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.matchAllQuery());
            sourceBuilder.size(0); // 只获取总数，不返回文档内容
            
            request.source(sourceBuilder);
            SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            
            return response.getHits().getTotalHits().value;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}