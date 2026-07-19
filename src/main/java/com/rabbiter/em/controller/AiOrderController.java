package com.rabbiter.em.controller;

import com.rabbiter.em.common.Result;
import com.rabbiter.em.entity.AiOrder;
import com.rabbiter.em.service.AiOrderService;
import com.rabbiter.em.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiOrderController {

    @Resource
    private AiOrderService aiOrderService;

    @PostMapping("/add")
    public Result addAiOrder(@RequestBody AiOrder aiOrder){
        aiOrderService.save(aiOrder);
        return Result.success();
    }

    @GetMapping("/findAllOrder")
    public Result selectAll(){
        Long userid = Long.valueOf(UserHolder.getUser().getId());
        List<AiOrder> aiOrders = aiOrderService.findAll();
        return Result.success(aiOrders);
    }
}