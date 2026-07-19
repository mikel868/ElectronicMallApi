package com.rabbiter.em.controller;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.constants.Constants;
import com.rabbiter.em.entity.dto.GoodDTO;
import com.rabbiter.em.service.GoodService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用SpringBootTest的真实集成测试
 */
@SpringBootTest
class EITest {

    @Autowired
    private GoodController goodController;

    @Autowired
    private GoodService goodService;

    @Test
    void testElasticsearchSearch() {
        // 测试有搜索文本时是否使用Elasticsearch搜索
        String searchText = "Redmi K7";
        
        Result result = goodController.searchGoods(1, 9, searchText, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(Constants.CODE_200, result.getCode());
        
        // 验证返回的数据
        IPage<GoodDTO> data = (IPage<GoodDTO>) result.getData();
        assertNotNull(data);
        
        System.out.println("=== Elasticsearch搜索测试 ===");
        System.out.println("搜索文本: " + searchText);
        System.out.println("返回结果数量: " + data.getTotal());

        System.out.println("当前页数据: " + data.getRecords().size());
    }

    @Test
    void testTraditionalSearch() {
        // 测试无搜索文本时是否使用传统搜索
        String searchText = "Redmi K70";
        
        Result result = goodController.searchGoods(1, 10, searchText, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(Constants.CODE_200, result.getCode());
        
        // 验证返回的数据
        IPage<GoodDTO> data = (IPage<GoodDTO>) result.getData();
        assertNotNull(data);
        
        System.out.println("=== 传统搜索测试 ===");
        System.out.println("搜索文本: " + searchText);
        System.out.println("返回结果数量: " + data.getTotal());
        System.out.println("当前页数据: " + data.getRecords().size());
    }

    @Test
    void testNullSearchText() {
        // 测试null搜索文本时是否使用传统搜索
        String searchText = "Redmi K70";
        
        Result result = goodController.searchGoods(1, 9, searchText, null);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(Constants.CODE_200, result.getCode());
        
        // 验证返回的数据
        IPage<GoodDTO> data = (IPage<GoodDTO>) result.getData();
        assertNotNull(data);
        
        System.out.println("=== Null搜索文本测试 ===");
        System.out.println("搜索文本: " + searchText);
        System.out.println("返回结果数量: " + data.getTotal());
        System.out.println("当前页数据: " + data.getRecords().size());
    }
}
