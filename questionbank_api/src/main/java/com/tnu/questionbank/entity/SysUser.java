package com.tnu.questionbank.entity;

import lombok.Data;

@Data
public class SysUser {
    private Integer id;
    private String userNum;
    private String password;
    private String role;     // student, teacher, admin
    private String realName;
}