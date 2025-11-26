package com.tnu.questionbank.entity;

import lombok.Data;

@Data
public class ExamPaperQuestion {
    private Integer id;
    private Integer paperId;    // 试卷ID
    private Integer questionId; // 题目ID
    private Integer score;      // 本题分值
    private Integer orderNo;    // 在试卷中的顺序
}