package com.rabbiter.em.controller;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据同步控制器
 * 提供数据库与Elasticsearch之间的数据同步接口
 */
@RestController
@RequestMapping("/sync")
@Tag(name = "数据同步管理", description = "数据库与Elasticsearch数据同步相关接口")
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 同步所有商品数据到Elasticsearch
     * @return 同步结果
     */
    @PostMapping("/goods/all")
    @Operation(summary = "同步所有商品数据", description = "将数据库中的所有商品数据同步到Elasticsearch")
    public Result syncAllGoods() {
        try {
            String result = dataSyncService.syncAllGoods();
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("同步失败：" + e.getMessage());
        }
    }

    /**
     * 同步单个商品到Elasticsearch
     * @param goodId 商品ID
     * @return 同步结果
     */
    @PostMapping("/goods/{goodId}")
    @Operation(summary = "同步单个商品数据", description = "将指定商品数据同步到Elasticsearch")
    public Result syncSingleGood(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        try {
            String result = dataSyncService.syncSingleGood(goodId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("同步失败：" + e.getMessage());
        }
    }

    /**
     * 从Elasticsearch删除商品
     * @param goodId 商品ID
     * @return 删除结果
     */
    @DeleteMapping("/goods/{goodId}")
    @Operation(summary = "从Elasticsearch删除商品", description = "从Elasticsearch中删除指定商品")
    public Result deleteGoodFromElasticsearch(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        try {
            String result = dataSyncService.deleteGoodFromElasticsearch(goodId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新商品在Elasticsearch中的数据
     * @param goodId 商品ID
     * @return 更新结果
     */
    @PutMapping("/goods/{goodId}")
    @Operation(summary = "更新商品数据", description = "更新Elasticsearch中的商品数据")
    public Result updateGoodInElasticsearch(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        try {
            String result = dataSyncService.updateGoodInElasticsearch(goodId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 检查Elasticsearch连接状态
     * @return 连接状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查Elasticsearch连接状态", description = "检查Elasticsearch连接状态和索引信息")
    public Result checkElasticsearchConnection() {
        try {
            String result = dataSyncService.checkElasticsearchConnection();
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("检查连接失败：" + e.getMessage());
        }
    }

    /**
     * 创建Elasticsearch索引
     * @return 创建结果
     */
    @PostMapping("/index/create")
    @Operation(summary = "创建Elasticsearch索引", description = "创建商品搜索索引")
    public Result createIndex() {
        try {
            String result = dataSyncService.createIndex();
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("创建索引失败：" + e.getMessage());
        }
    }

    /**
     * 删除Elasticsearch索引
     * @return 删除结果
     */
    @DeleteMapping("/index")
    @Operation(summary = "删除Elasticsearch索引", description = "删除商品搜索索引")
    public Result deleteIndex() {
        try {
            String result = dataSyncService.deleteIndex();
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("删除索引失败：" + e.getMessage());
        }
    }
}