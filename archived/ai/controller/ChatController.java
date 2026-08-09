package com.rabbiter.em.ai.controller;

import com.rabbiter.em.ai.service.ConsultantService;
import com.rabbiter.em.user.entity.User;
import com.rabbiter.em.ai.tools.ReservationTools;
import com.rabbiter.em.shared.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

    @Autowired
    private ConsultantService consultantService;
    
    @Autowired
    private ReservationTools reservationTools;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(String memoryId, String message) {
        // 设置当前用户ID到ReservationTools中
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null) {
            reservationTools.setCurrentUserId(Long.valueOf(currentUser.getId()));
        }
        
        Flux<String> result = consultantService.chat(memoryId, message);
        return result;
    }
}