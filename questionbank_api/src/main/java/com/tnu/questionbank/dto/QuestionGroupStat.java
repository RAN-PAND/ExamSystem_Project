package com.tnu.questionbank.dto;

import lombok.Data;

@Data
public class QuestionGroupStat {

    // 题目ID
    private Integer questionId;

    // 该组学生中作答总次数
    private Long totalCount;

    // 该组学生中答对次数
    private Long correctCount;
}
