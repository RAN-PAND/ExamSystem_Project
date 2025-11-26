package com.tnu.questionbank.dto;

import com.tnu.questionbank.entity.Question;
import lombok.Data;
import java.util.List;

@Data
public class ExamPaperDetailDTO {
    private Integer id;
    private String name;
    private String description;
    private Integer totalScore;
    private Integer questionCount;

    // 题目明细列表
    private List<Question> questions;
}