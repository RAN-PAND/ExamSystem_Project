package com.tnu.questionbank.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitResultDTO {

    private Integer paperId;
    private Integer studentId;
    private Integer totalScore;     // 本次考试总分

    private List<QuestionResult> details;  // 每题结果

    @Data
    public static class QuestionResult {
        private Integer questionId;
        private String studentAnswer;
        private String correctAnswer;
        private Integer score;       // 本题得分
        private Boolean correct;     // 是否答对
    }
}