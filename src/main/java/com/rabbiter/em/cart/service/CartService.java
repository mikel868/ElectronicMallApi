package com.rabbiter.em.cart.service;

import com.rabbiter.em.cart.entity.Cart;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.cart.mapper.CartMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class CartService extends ServiceImpl<CartMapper, Cart> {

    @Resource
    private CartMapper cartMapper;

    public List<Map<String,Object>> selectByUserId(Long userId) {
         return cartMapper.selectByUserId(userId);
    }
}
