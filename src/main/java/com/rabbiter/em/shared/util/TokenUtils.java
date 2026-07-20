package com.rabbiter.em.shared.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.rabbiter.em.user.entity.User;

public class TokenUtils {

    public static String genToken(String userId, String username){
        String token = JWT.create()
                .withAudience(userId)
                .sign(Algorithm.HMAC256(username));
        return token;
    }

    public static User getCurrentUser(){
        try{
            return UserHolder.getUser();
        }catch (Exception e){
            return null;
        }
    }
}
