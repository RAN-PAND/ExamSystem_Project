package com.tnu.questionbank.controller;

import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.dto.ExamRecordSummaryDTO;
import com.tnu.questionbank.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam/record")
@CrossOrigin
public class ExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    /**
     * 学生查看自己的考试记录
     * GET /api/exam/record/my?studentId=1
     */
    @GetMapping("/my")
    public Result<List<ExamRecordSummaryDTO>> listMyRecords(@RequestParam Integer studentId) {
        List<ExamRecordSummaryDTO> list = examRecordService.listByStudent(studentId);
        return Result.success(list);
    }
}
