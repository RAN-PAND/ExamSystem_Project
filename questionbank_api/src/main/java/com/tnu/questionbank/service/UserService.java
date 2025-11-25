package com.tnu.questionbank.service;

import com.tnu.questionbank.entity.SysUser;
import com.tnu.questionbank.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public SysUser login(String userNum, String password) {
        SysUser user = userMapper.findByUserNum(userNum);
        if (user == null) {
            throw new RuntimeException("该账号不存在");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!md5Password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return user;
    }

    public void register(SysUser user) {
        // --- 1. 安全校验：禁止注册管理员 ---
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("非法操作：不支持自主注册管理员账号！");
        }

        // --- 2. 密码加密 ---
        String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());

        // --- 3. 准备参数 ---
        Map<String, Object> params = new HashMap<>();
        params.put("userNum", user.getUserNum());
        params.put("password", md5Password);

        // 如果前端没传角色，默认是student
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            params.put("role", user.getRole());
        } else {
            params.put("role", "student");
        }

        params.put("realName", user.getRealName());
        params.put("result", 0);

        // --- 4. 调用存储过程 ---
        userMapper.registerUser(params);

        if ((Integer) params.get("result") == -1) {
            throw new RuntimeException("该学号/工号已存在");
        }
    }
}