package com.tnu.questionbank.dto;

import lombok.Data;

@Data
public class QuestionStat {

    // 题目ID
    private Integer questionId;

    // 总作答次数
    private Long totalCount;

    // 答对次数
    private Long correctCount;
}
