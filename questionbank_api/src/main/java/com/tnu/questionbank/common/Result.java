package com.tnu.questionbank.common;

import lombok.Data;

/**
 * 统一返回结果类
 * 无论是登录成功、查询失败还是报错，都返回这个对象给前端
 * 这是前后端分离开发的标准规范
 */
@Data
public class Result<T> {
    private String code; // 状态码: "200"成功, "500"失败
    private String msg;  // 提示信息: "登录成功", "密码错误"
    private T data;      // 返回的数据: 比如用户信息、题目列表

    // 成功时调用的方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode("200");
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    // 失败时调用的方法
    public static <T> Result<T> error(String code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}