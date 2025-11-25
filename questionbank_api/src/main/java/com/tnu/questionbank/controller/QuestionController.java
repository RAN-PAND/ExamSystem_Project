package com.tnu.questionbank.controller;

import com.github.pagehelper.PageInfo;
import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.entity.Question;
import com.tnu.questionbank.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@CrossOrigin
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // 分页查询接口
    // 修改 list 方法
    @GetMapping("/list")
    public Result<PageInfo<Question>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Integer knowledgeId,      // ✅ 新增
            @RequestParam(required = false) Integer difficultyLevel) { // ✅ 新增
        PageInfo<Question> pageInfo = questionService.findByPage(pageNum, pageSize,
                content, knowledgeId, difficultyLevel);
        return Result.success(pageInfo);
    }

    // 新增
    @PostMapping("/add")
    public Result<?> add(@RequestBody Question question) {
        questionService.add(question);
        return Result.success("添加成功");
    }

    // 修改
    @PutMapping("/update")
    public Result<?> update(@RequestBody Question question) {
        questionService.update(question);
        return Result.success("修改成功");
    }

    // 删除
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        questionService.delete(id);
        return Result.success("删除成功");
    }

    // 批量删除
    @DeleteMapping("/batchDelete")
    public Result<?> batchDelete(@RequestBody List<Integer> ids) {
        questionService.batchDelete(ids);
        return Result.success("批量删除成功");
    }

    // Excel 导入题库
    @PostMapping("/import")
    public Result<?> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            questionService.importExcel(file);
            return Result.success("");
        } catch (Exception e) {
            return Result.error("500", e.getMessage());
        }
    }

    // Excel 
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        questionService.exportExcel(response);
    }

    // P 
    @PostMapping("/recalcP")
    public Result<?> recalcP() {
        questionService.recalcDifficultyP();
        return Result.success("P");
    }

    // D 
    @PostMapping("/recalcD")
    public Result<?> recalcD() {
        questionService.recalcDiscriminationD();
        return Result.success("D");
    }

}