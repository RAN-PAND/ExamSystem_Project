package com.tnu.questionbank.entity;

import lombok.Data;

/**
 * 知识点实体类
 * 对应数据库表：course_knowledge
 */
@Data
public class CourseKnowledge {
    private Integer id;           // 主键ID
    private String name;          // 知识点名称
    private String target;        // 教学目标（了解/理解/掌握/应用）
    private String chapter;       // 所属章节
}
