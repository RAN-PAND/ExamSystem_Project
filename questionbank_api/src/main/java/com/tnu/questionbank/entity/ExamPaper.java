package com.tnu.questionbank.entity;

import lombok.Data;
import java.util.Date;

@Data
public class ExamPaper {
    private Integer id;
    private String name;           // 试卷名称
    private String description;    // 试卷说明
    private Integer totalScore;    // 总分
    private Integer questionCount; // 题目数量
    private Date createTime;       // 创建时间
    private Date updateTime;       // 最后更新时间
}