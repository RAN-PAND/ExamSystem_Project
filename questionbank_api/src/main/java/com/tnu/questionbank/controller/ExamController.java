package com.tnu.questionbank.controller;

import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.dto.ExamPaperViewDTO;
import com.tnu.questionbank.dto.ExamSubmitDTO;
import com.tnu.questionbank.dto.ExamSubmitResultDTO;
import com.tnu.questionbank.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin
public class ExamController {

    @Autowired
    private ExamService examService;

    // 获取试卷考试视图（不含正确答案）
    @GetMapping("/paper/{paperId}")
    public Result<ExamPaperViewDTO> getPaper(@PathVariable Integer paperId) {
        return Result.success(examService.getExamPaperView(paperId));
    }

    // 提交考试作答
    @PostMapping("/submit")
    public Result<ExamSubmitResultDTO> submit(@RequestBody ExamSubmitDTO submitDTO) {
        ExamSubmitResultDTO result = examService.submitExam(submitDTO);
        return Result.success(result);
    }
}