package com.tnu.questionbank.controller;

import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.entity.ExamPaper;
import com.tnu.questionbank.entity.ExamPaperQuestion;
import com.tnu.questionbank.dto.ExamPaperDetailDTO;
import com.tnu.questionbank.service.ExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
@CrossOrigin
public class ExamPaperController {

    @Autowired
    private ExamPaperService examPaperService;

    // 试卷列表（简单版：不分页）
    @GetMapping("/list")
    public Result<List<ExamPaper>> list() {
        return Result.success(examPaperService.listAll());
    }

    // 保存试卷及题目列表（新增/编辑）
    @PostMapping("/save")
    public Result<?> save(@RequestBody PaperSaveRequest request) {
        examPaperService.savePaperWithQuestions(request.getPaper(), request.getQuestionList());
        return Result.success("保存成功");
    }

    // 删除试卷
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        examPaperService.deletePaper(id);
        return Result.success("删除成功");
    }

    // 查询试卷详情
    @GetMapping("/detail/{id}")
    public Result<ExamPaperDetailDTO> detail(@PathVariable Integer id) {
        return Result.success(examPaperService.getPaperDetail(id));
    }

    // 内部请求体类，可单独放到 dto 包中
    public static class PaperSaveRequest {
        private ExamPaper paper;
        private List<ExamPaperQuestion> questionList;

        public ExamPaper getPaper() {
            return paper;
        }

        public void setPaper(ExamPaper paper) {
            this.paper = paper;
        }

        public List<ExamPaperQuestion> getQuestionList() {
            return questionList;
        }

        public void setQuestionList(List<ExamPaperQuestion> questionList) {
            this.questionList = questionList;
        }
    }
}