package com.rabbiter.em.income.controller;



import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.income.service.IncomeService;
import com.rabbiter.em.shared.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

@Authority(AuthorityType.requireAuthority)
@RestController
@RequestMapping("/api/income")
public class IncomeController {

    @Resource
    private IncomeService incomeService;

    @GetMapping("/chart")
    public Result getChart(){
        return Result.success(incomeService.getChart());
    }
    @GetMapping("/week")
    public Result getWeekIncome(){
        return Result.success(incomeService.getWeekIncome());
    }

    @GetMapping("/month")
    public Result getMonthIncome(){
        return Result.success(incomeService.getMonthIncome());
    }
}
