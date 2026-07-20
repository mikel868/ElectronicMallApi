package com.rabbiter.em.product.controller;

import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.product.entity.Category;
import com.rabbiter.em.product.service.CategoryService;
import com.rabbiter.em.shared.util.BaseApi;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    /*
    查询
    */
    @Authority(AuthorityType.noRequire)
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @GetMapping
    public Result findAll() {
        List<Category> list = categoryService.list();
        return Result.success(list);
    }

    /*
    保存
    */
    @PostMapping
    public Result save(@RequestBody Category category) {
        categoryService.saveOrUpdate(category);
        return Result.success();
    }

    /**
     *  新增下级分类 + 上下级分类关联
     *
     * @param category 下级分类
     * @return 结果
     */
    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Category category) {
        categoryService.add(category);
        return BaseApi.success();
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping
    public Result update(@RequestBody Category category) {
        categoryService.updateById(category);
        return Result.success();
    }


    /**
     * 删除分类
     *
     * @param id id
     * @return 结果
     */
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/delete")
    public Map<String, Object> delete(@RequestParam("id") Long id) {
        return categoryService.delete(id);
    }





}
