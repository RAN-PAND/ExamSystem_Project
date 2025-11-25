package com.tnu.questionbank.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 题库 Excel 导入导出 DTO
 */
@Data
public class QuestionExcelDTO {

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("选项A")
    private String optionA;

    @ExcelProperty("选项B")
    private String optionB;

    @ExcelProperty("选项C")
    private String optionC;

    @ExcelProperty("选项D")
    private String optionD;

    @ExcelProperty("正确答案")
    private String answer;

    @ExcelProperty("预设难度")
    private Integer difficultyLevel;

    @ExcelProperty("关联知识点ID")
    private Integer knowledgeId;
}