package com.rabbiter.em.system.controller;

import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.service.DataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据同步控制器
 * 提供数据库与Elasticsearch之间的数据同步接口
 *
 * Elasticsearch 未启用（app.es.enabled=false）时，
 * 所有接口统一返回「Elasticsearch 未启用，操作已跳过」。
 */
@RestController
@RequestMapping("/sync")
@Tag(name = "数据同步管理", description = "数据库与Elasticsearch数据同步相关接口")
public class DataSyncController {

    @Autowired(required = false)
    private DataSyncService dataSyncService;

    @PostMapping("/goods/all")
    @Operation(summary = "同步所有商品数据", description = "将数据库中的所有商品数据同步到Elasticsearch")
    public Result syncAllGoods() {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.syncAllGoods());
        } catch (Exception e) {
            return Result.fail("同步失败：" + e.getMessage());
        }
    }

    @PostMapping("/goods/{goodId}")
    @Operation(summary = "同步单个商品数据", description = "将指定商品数据同步到Elasticsearch")
    public Result syncSingleGood(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.syncSingleGood(goodId));
        } catch (Exception e) {
            return Result.fail("同步失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/goods/{goodId}")
    @Operation(summary = "从Elasticsearch删除商品", description = "从Elasticsearch中删除指定商品")
    public Result deleteGoodFromElasticsearch(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.deleteGoodFromElasticsearch(goodId));
        } catch (Exception e) {
            return Result.fail("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/goods/{goodId}")
    @Operation(summary = "更新商品数据", description = "更新Elasticsearch中的商品数据")
    public Result updateGoodInElasticsearch(
            @Parameter(description = "商品ID") @PathVariable Long goodId) {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.updateGoodInElasticsearch(goodId));
        } catch (Exception e) {
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/status")
    @Operation(summary = "检查Elasticsearch连接状态", description = "检查Elasticsearch连接状态和索引信息")
    public Result checkElasticsearchConnection() {
        if (dataSyncService == null) {
            Map<String, Object> status = new HashMap<>();
            status.put("enabled", false);
            status.put("connected", false);
            status.put("message", "Elasticsearch 未启用");
            return Result.success(status);
        }
        try {
            return Result.success(dataSyncService.checkElasticsearchConnection());
        } catch (Exception e) {
            return Result.fail("检查连接失败：" + e.getMessage());
        }
    }

    @PostMapping("/index/create")
    @Operation(summary = "创建Elasticsearch索引", description = "创建商品搜索索引")
    public Result createIndex() {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.createIndex());
        } catch (Exception e) {
            return Result.fail("创建索引失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/index")
    @Operation(summary = "删除Elasticsearch索引", description = "删除商品搜索索引")
    public Result deleteIndex() {
        if (dataSyncService == null) return esDisabled();
        try {
            return Result.success(dataSyncService.deleteIndex());
        } catch (Exception e) {
            return Result.fail("删除索引失败：" + e.getMessage());
        }
    }

    private Result esDisabled() {
        return Result.success("Elasticsearch 未启用，数据同步已跳过");
    }
}
