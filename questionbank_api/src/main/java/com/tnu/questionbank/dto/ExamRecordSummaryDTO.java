package com.tnu.questionbank.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRecordSummaryDTO {

    private Integer paperId;
    private String paperName;

    private Integer studentId;
    private String studentName;     // 预留字段，如需要可关联 sys_user

    private Integer totalScore;     // 本次考试总分
    private LocalDateTime submitTime;
}
