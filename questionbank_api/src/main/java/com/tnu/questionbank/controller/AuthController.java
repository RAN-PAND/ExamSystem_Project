package com.tnu.questionbank.controller;

import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.entity.SysUser;
import com.tnu.questionbank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController // 标记这是个接口控制器，返回JSON数据
@RequestMapping("/api") // 所有接口都以 /api 开头
@CrossOrigin // 允许跨域 (非常重要！允许Vue访问后端)
public class AuthController {

    @Autowired
    private UserService userService;

    // 登录接口: POST /api/login
    // @RequestBody 接收前端传来的 JSON 数据
    @PostMapping("/login")
    public Result<SysUser> login(@RequestBody SysUser loginForm) {
        try {
            // 调用业务层
            SysUser user = userService.login(loginForm.getUserNum(), loginForm.getPassword());
            // 登录成功，返回数据
            return Result.success(user);
        } catch (Exception e) {
            // 登录失败，返回错误信息
            return Result.error("500", e.getMessage());
        }
    }
    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser registerForm) {
        try {
            userService.register(registerForm);
            return Result.success("注册成功");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }
}