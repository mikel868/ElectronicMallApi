package com.rabbiter.em.product.controller;

import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.product.dto.GoodDocument;
import com.rabbiter.em.system.service.ElasticsearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 搜索控制器
 * 提供商品搜索相关的API接口
 */
@RestController
@RequestMapping("/search")
@Tag(name = "搜索管理", description = "商品搜索相关接口")
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    /**
     * 关键词搜索商品
     * @param keyword 搜索关键词
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/goods")
    @Operation(summary = "关键词搜索商品", description = "根据关键词搜索商品，支持商品名称和描述")
    public Result searchGoods(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            List<GoodDocument> result = elasticsearchService.searchGoods(keyword, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据商品名称搜索
     * @param name 商品名称
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/goods/name")
    @Operation(summary = "根据商品名称搜索", description = "根据商品名称进行精确搜索")
    public Result searchGoodsByName(
            @Parameter(description = "商品名称") @RequestParam String name,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByName(name, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类搜索商品
     * @param categoryId 分类ID
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/goods/category")
    @Operation(summary = "根据分类搜索商品", description = "根据商品分类ID搜索商品")
    public Result searchGoodsByCategory(
            @Parameter(description = "分类ID") @RequestParam Long categoryId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByCategory(categoryId, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 搜索推荐商品
     * @param page 页码
     * @param size 每页大小
     * @return 搜索结果
     */
    @GetMapping("/goods/recommended")
    @Operation(summary = "搜索推荐商品", description = "获取所有推荐商品")
    public Result searchRecommendedGoods(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        try {
            List<GoodDocument> result = elasticsearchService.searchRecommendedGoods(page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有推荐商品（不分页）
     * @return 推荐商品列表
     */
    @GetMapping("/goods/recommended/all")
    @Operation(summary = "获取所有推荐商品", description = "获取所有推荐商品，不分页")
    public Result getAllRecommendedGoods() {
        try {
            List<GoodDocument> result = elasticsearchService.searchRecommendedGoods(0, 1000);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("获取推荐商品失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类获取所有商品（不分页）
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @GetMapping("/goods/category/all")
    @Operation(summary = "根据分类获取所有商品", description = "根据分类ID获取所有商品，不分页")
    public Result getAllGoodsByCategory(
            @Parameter(description = "分类ID") @RequestParam Long categoryId) {
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByCategory(categoryId, 0, 1000);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("获取商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取搜索统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取搜索统计信息", description = "获取Elasticsearch索引的统计信息")
    public Result getSearchStats() {
        try {
            boolean indexExists = elasticsearchService.indexExists();
            long documentCount = elasticsearchService.getDocumentCount();
            
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("indexExists", indexExists);
            stats.put("documentCount", documentCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            return Result.fail("获取统计信息失败：" + e.getMessage());
        }
    }
}