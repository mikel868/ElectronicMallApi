package com.rabbiter.em.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.em.ai.entity.AiOrder;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AiOrderMapper extends BaseMapper<AiOrder> {


    @Select("select * from ai_order where user_id = #{userid}")
    List<AiOrder> findAll(Long userid);
    
    @Select("select * from ai_order where order_no = #{orderNo}")
    AiOrder findByOrderNo(String orderNo);
}