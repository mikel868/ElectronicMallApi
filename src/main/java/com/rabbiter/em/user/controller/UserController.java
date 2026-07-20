package com.rabbiter.em.user.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.em.shared.annotation.Authority;
import com.rabbiter.em.shared.constants.Constants;
import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.system.entity.AuthorityType;
import com.rabbiter.em.user.entity.LoginForm;
import com.rabbiter.em.user.entity.User;
import com.rabbiter.em.shared.dto.UserDTO;
import com.rabbiter.em.user.service.UserService;
import com.rabbiter.em.shared.util.TokenUtils;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
这个注解表示该控制器下所有接口都可以通过跨域访问，注解内可以指定某一域名
也可以配置config类
 */
@CrossOrigin
@RestController
public class UserController {
    @Autowired
    private UserService userService;


    @PostMapping("/login")
    public Result login(@RequestBody LoginForm loginForm) {
        UserDTO dto = userService.login(loginForm);
        return Result.success(dto);
    }

  
    @PostMapping("/register")
    public Result register(@RequestBody LoginForm loginForm) {
        User user = userService.register(loginForm);
        return Result.success(user);
    }

    @GetMapping("/userinfo/{username}")
    public Result getUserInfoByName(@PathVariable String username) {
        User one = userService.getOne(username);
        return Result.success(one);
    }

    @GetMapping("/userid")
    public long getUserId() {
        return TokenUtils.getCurrentUser().getId();
    }

    @GetMapping("/user/")
    public Result findAll() {
        List<User> list = userService.list();
        return Result.success(list);
    }

    @PostMapping("/user")
    public Result save(@RequestBody User user) {

        return userService.saveUpdate(user);
    }

    @Authority(AuthorityType.requireAuthority)
    @DeleteMapping("/user/{id}")
    public Result deleteById(@PathVariable int id) {
        boolean isSuccessful = userService.removeById(id);
        if (isSuccessful) {
            return Result.success();
        } else {
            return Result.error(Constants.CODE_500, "删除失败");
        }
    }

    @Authority(AuthorityType.requireAuthority)
    @PostMapping("/user/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        boolean isSuccessful = userService.removeBatchByIds(ids);
        if (isSuccessful) {
            return Result.success();
        } else {
            return Result.error(Constants.CODE_500, "删除失败");
        }
    }

    @GetMapping("/user/page")
    public Result findPage(@RequestParam int pageNum,
                           @RequestParam int pageSize,
                           String id,
                           String username,
                           String nickname) {
        IPage<User> userPage = new Page<>(pageNum, pageSize);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (!StrUtil.isEmpty(id)) {
            userQueryWrapper.like("id", id);
        }
        if (!StrUtil.isEmpty(username)) {
            userQueryWrapper.like("username", username);
        }
        if (!StrUtil.isEmpty(nickname)) {
            userQueryWrapper.like("nickname", nickname);
        }
        userQueryWrapper.orderByDesc("id");
        System.out.println("============" + TokenUtils.getCurrentUser());
        return Result.success(userService.page(userPage, userQueryWrapper));
    }

    /**
     * 重置密码
     *
     * @param id          用户id
     * @param newPassword 新密码
     * @return 结果
     */
    @GetMapping("/user/resetPassword")
    public Result resetPassword(@RequestParam String id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}
