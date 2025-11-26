package com.tnu.questionbank.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExamSubmitDTO {

    private Integer studentId;   // 学生ID（演示用）
    private Integer paperId;     // 试卷ID

    private List<AnswerItem> answers;  // 每题作答情况

    @Data
    public static class AnswerItem {
        private Integer questionId; // 题目ID
        private String answer;      // 学生选择的选项，如 "A"
    }
}