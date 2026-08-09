package com.rabbiter.em.ai.controller;

import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.ai.entity.AiOrder;
import com.rabbiter.em.ai.service.AiOrderService;
import com.rabbiter.em.shared.util.UserHolder;
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