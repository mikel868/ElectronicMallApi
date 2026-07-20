package com.rabbiter.em.order.service;

import com.rabbiter.em.order.entity.OrderGoods;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.order.mapper.OrderGoodsMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class OrderGoodsService extends ServiceImpl<OrderGoodsMapper, OrderGoods> {

    @Resource
    private OrderGoodsMapper orderGoodsMapper;

}
