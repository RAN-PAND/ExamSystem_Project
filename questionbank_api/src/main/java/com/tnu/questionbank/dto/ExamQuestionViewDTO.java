package com.tnu.questionbank.dto;

import lombok.Data;

@Data
public class ExamQuestionViewDTO {
    private Integer id;         // 题目ID
    private String content;     // 题干
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private Integer questionType; // 题型：1-单选,2-多选,3-判断,4-填空,5-简答
    private Integer score;      // 本题分值
    private Integer orderNo;    // 在试卷中的顺序
}