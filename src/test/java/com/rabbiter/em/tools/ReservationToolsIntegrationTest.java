package com.rabbiter.em.tools;

import com.rabbiter.em.entity.Good;
import com.rabbiter.em.mapper.GoodMapper;
import com.rabbiter.em.service.GoodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest  // 需要这个注解来启动Spring容器
class GoodQueryTest {

    @Autowired  // 自动注入真实的GoodService
    private GoodService goodService;
    @Autowired
    private GoodMapper goodMapper;

    @Test
    void testFindGoodByNameAndNotDeleted() {
        // 测试数据 - 使用数据库中实际存在的商品名称
        String goodName = "Redmi K70";
        String standard = "月白/8+256G";
        // 确保这个商品在数据库中存在

        // 执行查询
        Good good = goodService.lambdaQuery()
                .eq(Good::getName, goodName)
                .eq(Good::getIsDelete, false)
                .one();

        long id = good.getId();
        BigDecimal price =goodService.getStandardPrice(standard,id);

        // 打印价格
        System.out.println("商品 '" + goodName + "' 的价格是: " + price);

    }


}