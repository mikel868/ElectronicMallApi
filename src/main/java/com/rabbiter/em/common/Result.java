package com.rabbiter.em.common;

import com.rabbiter.em.constants.Constants;

public class Result {

    private String code;
    private String msg;
    private Object data;

    public static Result success(){
        return new Result(Constants.CODE_200,null,null);
    }

    public static Result success(Object data){
        return new Result(Constants.CODE_200,null,data);
    }
    public static Result error(){
        return new Result(Constants.CODE_500,null,null);
    }
    public static Result error(String code,String msg){
        return new Result(code,msg,null);
    }

    public static Result success(Object data, String msg) {
          return new Result(Constants.CODE_200, msg, data);
    }

    public static Result fail(String s) {
        return new Result(Constants.CODE_500, s, null);
    }

    public static Result ok(long orderId) {
        return new Result(Constants.CODE_200, "下单成功", orderId);
    }


    public Result code(String code){
        this.code = code;
        return this;
    }
    public Result message(String msg){
        this.msg = msg;
        return this;
    }

    public Result() {
    }

    public Result(String code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
