package com.rabbiter.em.product.controller;

import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.product.entity.Good;
import com.rabbiter.em.product.entity.Standard;
import com.rabbiter.em.product.service.GoodService;
import com.rabbiter.em.product.service.StandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/good")
public class GoodController {
    @Resource
    private GoodService goodService;

    @Resource
    private StandardService standardService;



    @Authority(AuthorityType.requireAuthority)
    @PostMapping
    public Result save(@RequestBody Good good) {
        System.out.println(good);
        return Result.success(goodService.saveOrUpdateGood(good));
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping
    public Result update(@RequestBody Good good) {
        goodService.update(good);
        return Result.success();
    }

    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        goodService.deleteGood(id);
        return Result.success();
    }

    @Authority(AuthorityType.noRequire)
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        return Result.success(goodService.getGoodById(id));
    }

    //获取商品的规格信息
    @Authority(AuthorityType.noRequire)
    @GetMapping("/standard/{id}")
    public Result getStandard(@PathVariable int id) {
        return Result.success(goodService.getStandard(id));
    }
    //查询推荐商品，即recommend=1
    @Authority(AuthorityType.noRequire)
    @GetMapping
    public Result findAll() {

        return Result.success(goodService.findFrontGoods());
    }
    //查询销量排行
    @Authority(AuthorityType.noRequire)
    @GetMapping("/rank")
    public Result getSaleRank(@RequestParam int num){
        return Result.success(goodService.getSaleRank(num));
    }
    //保存商品的规格信息
    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/standard")
    public Result saveStandard(@RequestBody List<Standard> standards, @RequestParam int goodId) {
        //先删除全部旧记录
        standardService.deleteAll(goodId);
        //然后插入新记录
        for (Standard standard : standards) {
            standard.setGoodId(goodId);
            if(!standardService.save(standard)){
                return Result.error(Constants.CODE_500,"保存失败");
            }
        }
        return Result.success();
    }

    //删除商品的规格信息
    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/standard")
    public Result delStandard(@RequestBody Standard standard) {
        boolean delete = standardService.delete(standard);
        if(delete) {
            return Result.success();
        }else {
            return Result.error(Constants.CODE_500,"删除失败");
        }
    }

    //修改商品的推荐字段
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/recommend")
    public Result setRecommend(@RequestParam Long id,@RequestParam Boolean isRecommend){
        return Result.success(goodService.setRecommend(id,isRecommend));
    }

    @Authority(AuthorityType.noRequire)
    @GetMapping("/page")
    public Result findPage(
                            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
                            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false, defaultValue = "") String searchText,
                            @RequestParam(required = false) Integer categoryId) {

        return Result.success(goodService.findPage(pageNum,pageSize,searchText,categoryId));
    }

    @Authority(AuthorityType.noRequire)
    @GetMapping("/search")
    public Result searchGoods(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String searchText,
            @RequestParam(required = false) Integer categoryId) {


       // 如果有搜索文本，使用 Elasticsearch 搜索
        if (searchText != null && !searchText.isEmpty()) {
            log.info("使用Elasticsearch搜索");
            return Result.success(goodService.searchGoodsWithElasticsearch(pageNum, pageSize, searchText, categoryId));
        } else {
             //否则使用传统搜索
            log.info("使用传统搜索");
            return Result.success(goodService.findPage(pageNum, pageSize, searchText, categoryId));
        }
    }
    
    @Authority(AuthorityType.noRequire)
    @GetMapping("/fullPage")
    public Result findFullPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String searchText,
            @RequestParam(required = false) Integer categoryId) {

        return Result.success(goodService.findFullPage(pageNum,pageSize,searchText,categoryId));
    }



}
