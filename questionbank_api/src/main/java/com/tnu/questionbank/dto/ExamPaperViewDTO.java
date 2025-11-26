package com.tnu.questionbank.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamPaperViewDTO {
    private Integer paperId;
    private String paperName;
    private String description;
    private Integer totalScore;
    private Integer questionCount;

    private List<ExamQuestionViewDTO> questions;
}