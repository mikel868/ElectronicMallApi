package com.rabbiter.em.user.controller;

import com.rabbiter.em.shared.result.Result;
import com.rabbiter.em.user.entity.User;
import com.rabbiter.em.shared.util.TokenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {
    @PostMapping("/role")
    public Result getUserRole(){
        User currentUser = TokenUtils.getCurrentUser();
        return Result.success(currentUser.getRole());
    }
}
