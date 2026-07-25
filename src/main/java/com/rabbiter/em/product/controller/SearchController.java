package com.rabbiter.em.product.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.product.dto.GoodDocument;
import com.rabbiter.em.product.dto.GoodDTO;
import com.rabbiter.em.product.service.GoodService;
import com.rabbiter.em.system.service.ElasticsearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索控制器
 * 提供商品搜索相关的API接口
 *
 * Elasticsearch 未启用（app.es.enabled=false）时，
 * 所有接口自动回退到 MySQL LIKE 查询，保持响应结构一致。
 */
@RestController
@RequestMapping("/search")
@Tag(name = "搜索管理", description = "商品搜索相关接口")
public class SearchController {

    @Autowired(required = false)
    private ElasticsearchService elasticsearchService;

    @Autowired
    private GoodService goodService;

    /**
     * 关键词搜索商品
     */
    @GetMapping("/goods")
    @Operation(summary = "关键词搜索商品", description = "根据关键词搜索商品，支持商品名称和描述")
    public Result searchGoods(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        if (elasticsearchService == null) {
            return Result.success(searchByMysql(keyword, null, page, size));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchGoods(keyword, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据商品名称搜索
     */
    @GetMapping("/goods/name")
    @Operation(summary = "根据商品名称搜索", description = "根据商品名称进行精确搜索")
    public Result searchGoodsByName(
            @Parameter(description = "商品名称") @RequestParam String name,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        if (elasticsearchService == null) {
            return Result.success(searchByMysql(name, null, page, size));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByName(name, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类搜索商品
     */
    @GetMapping("/goods/category")
    @Operation(summary = "根据分类搜索商品", description = "根据商品分类ID搜索商品")
    public Result searchGoodsByCategory(
            @Parameter(description = "分类ID") @RequestParam Long categoryId,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        if (elasticsearchService == null) {
            return Result.success(searchByMysql(null, categoryId, page, size));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByCategory(categoryId, page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 搜索推荐商品
     */
    @GetMapping("/goods/recommended")
    @Operation(summary = "搜索推荐商品", description = "获取所有推荐商品")
    public Result searchRecommendedGoods(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int size) {
        if (elasticsearchService == null) {
            return Result.success(recommendedByMysql(page, size));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchRecommendedGoods(page * size, size);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有推荐商品（不分页）
     */
    @GetMapping("/goods/recommended/all")
    @Operation(summary = "获取所有推荐商品", description = "获取所有推荐商品，不分页")
    public Result getAllRecommendedGoods() {
        if (elasticsearchService == null) {
            return Result.success(recommendedByMysql(0, 1000));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchRecommendedGoods(0, 1000);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("获取推荐商品失败：" + e.getMessage());
        }
    }

    /**
     * 根据分类获取所有商品（不分页）
     */
    @GetMapping("/goods/category/all")
    @Operation(summary = "根据分类获取所有商品", description = "根据分类ID获取所有商品，不分页")
    public Result getAllGoodsByCategory(
            @Parameter(description = "分类ID") @RequestParam Long categoryId) {
        if (elasticsearchService == null) {
            return Result.success(searchByMysql(null, categoryId, 0, 1000));
        }
        try {
            List<GoodDocument> result = elasticsearchService.searchGoodsByCategory(categoryId, 0, 1000);
            return Result.success(result);
        } catch (Exception e) {
            return Result.fail("获取商品失败：" + e.getMessage());
        }
    }

    /**
     * 获取搜索统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取搜索统计信息", description = "获取Elasticsearch索引的统计信息")
    public Result getSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        if (elasticsearchService == null) {
            stats.put("enabled", false);
            stats.put("indexExists", false);
            stats.put("documentCount", 0L);
            return Result.success(stats);
        }
        try {
            boolean indexExists = elasticsearchService.indexExists();
            long documentCount = elasticsearchService.getDocumentCount();
            stats.put("enabled", true);
            stats.put("indexExists", indexExists);
            stats.put("documentCount", documentCount);
            return Result.success(stats);
        } catch (Exception e) {
            return Result.fail("获取统计信息失败：" + e.getMessage());
        }
    }

    // ===================== MySQL 兜底查询 =====================

    /**
     * 关键词 + 分类 MySQL 模糊查询，封装为 GoodDocument 列表
     */
    private List<GoodDocument> searchByMysql(String keyword, Long categoryId, int page, int size) {
        Integer categoryInt = categoryId == null ? null : categoryId.intValue();
        IPage<GoodDTO> result = goodService.findPage(page + 1, size, keyword, categoryInt);
        return result.getRecords().stream()
                .map(this::dtoToDocument)
                .collect(Collectors.toList());
    }

    /**
     * 推荐商品 MySQL 查询（GoodMapper.findFrontGoods 已是推荐商品查询）
     */
    private List<GoodDocument> recommendedByMysql(int page, int size) {
        List<GoodDTO> all = goodService.findFrontGoods();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        return all.subList(from, to).stream()
                .map(this::dtoToDocument)
                .collect(Collectors.toList());
    }

    private GoodDocument dtoToDocument(GoodDTO dto) {
        GoodDocument doc = new GoodDocument();
        BeanUtil.copyProperties(dto, doc);
        return doc;
    }
}
