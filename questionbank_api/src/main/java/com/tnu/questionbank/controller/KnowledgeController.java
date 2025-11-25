package com.tnu.questionbank.controller;

import com.tnu.questionbank.common.Result;
import com.tnu.questionbank.entity.CourseKnowledge;
import com.tnu.questionbank.service.KnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@CrossOrigin
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    // 查询所有知识点（支持搜索）
    @GetMapping("/list")
    public Result<List<CourseKnowledge>> list(@RequestParam(required = false) String name) {
        List<CourseKnowledge> list = knowledgeService.findAll(name);
        return Result.success(list);
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public Result<CourseKnowledge> getById(@PathVariable Integer id) {
        CourseKnowledge knowledge = knowledgeService.findById(id);
        return Result.success(knowledge);
    }

    // 新增知识点
    @PostMapping("/add")
    public Result<?> add(@RequestBody CourseKnowledge knowledge) {
        knowledgeService.add(knowledge);
        return Result.success("添加成功");
    }

    // 修改知识点
    @PutMapping("/update")
    public Result<?> update(@RequestBody CourseKnowledge knowledge) {
        knowledgeService.update(knowledge);
        return Result.success("修改成功");
    }

    // 删除知识点
    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        knowledgeService.delete(id);
        return Result.success("删除成功");
    }
}