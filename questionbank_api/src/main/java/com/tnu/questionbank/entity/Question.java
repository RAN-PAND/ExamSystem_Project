package com.tnu.questionbank.entity;

import lombok.Data;

/**
 * 对应数据库表：question_bank
 * 使用 @Data 注解自动生成 Getters, Setters, toString 等
 */
@Data
public class Question {
    private Integer id;
    private String content;      // 题干
    private String optionA;      // 选项A
    private String optionB;
    private String optionC;
    private String optionD;
    private String answer;       // 正确答案
    private Integer scoreDefault;// 缺省分值

    // --- 核心加分字段 ---
    private Integer knowledgeId; // 关联知识点
    private Integer difficultyLevel; // 静态难度(1-4)
    private Float difficultyP;   // 动态难度P值
    private Float discriminationD;// 区分度D值
    private Integer exposureCount;// 曝光率

    // 题型：1-单选, 2-多选, 3-判断, 4-填空, 5-简答
    private Integer questionType;
}