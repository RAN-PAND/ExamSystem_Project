package com.tnu.questionbank.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ExamRecord {

    private Integer id;

    // 学生用户ID（关联 sys_user.id）
    private Integer studentId;

    // 题目ID（关联 question_bank.id）
    private Integer questionId;

    // 学生作答答案，如 A/B/C/D
    private String answer;

    // 是否作答正确：1-正确，0-错误
    private Integer isCorrect;

    // 本题得分
    private Integer score;

    // 提交时间
    private Date submitTime;
}