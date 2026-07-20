package com.rabbiter.em.product.controller;

import com.auth0.jwt.JWT;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.product.entity.Icon;
import com.rabbiter.em.user.entity.User;
import com.rabbiter.em.product.service.IconService;
import com.rabbiter.em.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/icon")
public class IconController {
    @Resource
    private IconService iconService;
    @Resource
    private HttpServletRequest request;
    @Resource
    private UserService userService;

    public User getUser() {
        String token = request.getHeader("token");
        String username = JWT.decode(token).getAudience().get(0);
        return userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    }

    /*
    查询
    */
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        return Result.success(iconService.getById(id));
    }

    @GetMapping
    public Result findAll() {
        List<Icon> list = iconService.getIconCategoryMapList();
        return Result.success(list);
    }


    /*
    保存
    */
    @Authority(AuthorityType.requireAuthority)
    @PostMapping
    public Result save(@RequestBody Icon icon) {
        iconService.saveOrUpdate(icon);
        return Result.success();
    }

    @Authority(AuthorityType.requireAuthority)
    @PutMapping
    public Result update(@RequestBody Icon icon) {
        iconService.updateById(icon);
        return Result.success();
    }

    /*
     *删除
     */
    @Authority(AuthorityType.requireAuthority)
    @GetMapping("/delete")
    public Map<String, Object> delete(@RequestParam("id") Long id) {
        return iconService.deleteById(id);
    }

}
