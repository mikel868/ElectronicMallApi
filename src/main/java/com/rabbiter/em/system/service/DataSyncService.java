package com.rabbiter.em.system.service;

import com.rabbiter.em.product.entity.Good;
import com.rabbiter.em.product.dto.GoodDocument;
import com.rabbiter.em.product.mapper.GoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据同步服务
 * 负责将数据库数据同步到Elasticsearch
 *
 * 通过 app.es.enabled 控制：false 时此类不实例化。
 */
@Service
@ConditionalOnProperty(prefix = "app.es", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSyncService {

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 同步所有商品数据到Elasticsearch
     * @return 同步结果
     */
    public String syncAllGoods() {
        try {
            // 确保索引存在
            if (!elasticsearchService.indexExists()) {
                elasticsearchService.createIndex();
            }

            // 从数据库获取所有商品
            List<Good> goods = goodMapper.selectList(null);
            
            if (goods == null || goods.isEmpty()) {
                return "没有找到商品数据";
            }

            int successCount = 0;
            int failCount = 0;

            // 逐个保存到Elasticsearch
            for (Good good : goods) {
                GoodDocument document = elasticsearchService.convertToDocument(good);
                if (elasticsearchService.saveGoodDocument(document)) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            return String.format("同步完成：成功 %d 条，失败 %d 条", successCount, failCount);
        } catch (Exception e) {
            return "同步失败：" + e.getMessage();
        }
    }

    /**
     * 同步单个商品到Elasticsearch
     * @param goodId 商品ID
     * @return 同步结果
     */
    public String syncSingleGood(Long goodId) {
        try {
            // 确保索引存在
            if (!elasticsearchService.indexExists()) {
                elasticsearchService.createIndex();
            }

            // 从数据库获取商品
            Good good = goodMapper.selectById(goodId);
            
            if (good == null) {
                return "商品不存在";
            }

            // 转换为GoodDocument并保存
            GoodDocument document = elasticsearchService.convertToDocument(good);
            if (elasticsearchService.saveGoodDocument(document)) {
                return "成功同步商品：" + good.getName();
            } else {
                return "同步商品失败：" + good.getName();
            }
        } catch (Exception e) {
            return "同步失败：" + e.getMessage();
        }
    }

    /**
     * 从Elasticsearch删除商品
     * @param goodId 商品ID
     * @return 删除结果
     */
    public String deleteGoodFromElasticsearch(Long goodId) {
        try {
            if (elasticsearchService.deleteGoodDocument(goodId)) {
                return "成功从Elasticsearch删除商品";
            } else {
                return "删除商品失败";
            }
        } catch (Exception e) {
            return "删除失败：" + e.getMessage();
        }
    }

    /**
     * 更新商品在Elasticsearch中的数据
     * @param goodId 商品ID
     * @return 更新结果
     */
    public String updateGoodInElasticsearch(Long goodId) {
        try {
            // 确保索引存在
            if (!elasticsearchService.indexExists()) {
                elasticsearchService.createIndex();
            }

            // 从数据库获取最新商品数据
            Good good = goodMapper.selectById(goodId);
            
            if (good == null) {
                return "商品不存在";
            }

            // 转换为GoodDocument并更新
            GoodDocument document = elasticsearchService.convertToDocument(good);
            if (elasticsearchService.saveGoodDocument(document)) {
                return "成功更新商品：" + good.getName();
            } else {
                return "更新商品失败：" + good.getName();
            }
        } catch (Exception e) {
            return "更新失败：" + e.getMessage();
        }
    }

    /**
     * 检查Elasticsearch连接状态
     * @return 连接状态
     */
    public String checkElasticsearchConnection() {
        try {
            boolean indexExists = elasticsearchService.indexExists();
            long documentCount = elasticsearchService.getDocumentCount();
            
            return String.format("Elasticsearch连接正常，索引存在：%s，文档数量：%d", 
                    indexExists ? "是" : "否", documentCount);
        } catch (Exception e) {
            return "Elasticsearch连接失败：" + e.getMessage();
        }
    }

    /**
     * 创建Elasticsearch索引
     * @return 创建结果
     */
    public String createIndex() {
        try {
            if (elasticsearchService.createIndex()) {
                return "成功创建Elasticsearch索引";
            } else {
                return "创建Elasticsearch索引失败";
            }
        } catch (Exception e) {
            return "创建索引失败：" + e.getMessage();
        }
    }

    /**
     * 删除Elasticsearch索引
     * @return 删除结果
     */
    public String deleteIndex() {
        try {
            if (elasticsearchService.deleteIndex()) {
                return "成功删除Elasticsearch索引";
            } else {
                return "删除Elasticsearch索引失败";
            }
        } catch (Exception e) {
            return "删除索引失败：" + e.getMessage();
        }
    }
}