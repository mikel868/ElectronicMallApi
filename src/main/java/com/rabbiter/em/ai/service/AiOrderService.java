package com.rabbiter.em.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbiter.em.ai.entity.AiOrder;
import com.rabbiter.em.ai.mapper.AiOrderMapper;

import com.rabbiter.em.shared.util.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AiOrderService extends ServiceImpl<AiOrderMapper, AiOrder> {


    @Resource
    private AiOrderMapper aiOrderMapper;
    public List<AiOrder> findAll() {
        Long userid = Long.valueOf(UserHolder.getUser().getId());
        return aiOrderMapper.findAll( userid );
    }
    
    // 根据用户ID查询预约订单
    public List<AiOrder> findByUserId(long userId) {
        return aiOrderMapper.findAll(userId);
    }

}