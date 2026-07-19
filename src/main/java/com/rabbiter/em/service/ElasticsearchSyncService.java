package com.rabbiter.em.service;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.entity.dto.GoodDocument;
import com.rabbiter.em.mapper.GoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Elasticsearch 同步服务
 * 负责将商品数据同步到 Elasticsearch
 * 使用新的 ElasticsearchService 替代已删除的 GoodRepository
 * 移除对 GoodService 的依赖以避免循环依赖
 */
@Service
public class ElasticsearchSyncService {

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 将商品实体转换为 Elasticsearch 文档
     * @param good 商品实体
     * @return 商品文档
     */
    public GoodDocument convertToDocument(Good good) {
        return elasticsearchService.convertToDocument(good);
    }

    /**
     * 同步单个商品到 Elasticsearch
     * @param good 商品实体
     */
    public void syncGoodToElasticsearch(Good good) {
        GoodDocument document = convertToDocument(good);
        elasticsearchService.saveGoodDocument(document);
    }

    /**
     * 同步所有商品到 Elasticsearch
     */
    public void syncAllGoodsToElasticsearch() {
        // 直接使用 GoodMapper 获取所有商品，避免循环依赖
        List<Good> goods = goodMapper.selectList(null);
        
        for (Good good : goods) {
            // 只同步未删除的商品
            if (good.getIsDelete() == null || !good.getIsDelete()) {
                GoodDocument document = convertToDocument(good);
                elasticsearchService.saveGoodDocument(document);
            }
        }
    }

    /**
     * 根据商品ID删除 Elasticsearch 中的商品文档
     * @param id 商品ID
     */
    public void deleteGoodFromElasticsearch(Long id) {
        elasticsearchService.deleteGoodDocument(id);
    }
}