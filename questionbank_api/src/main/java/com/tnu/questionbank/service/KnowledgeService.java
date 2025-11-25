package com.tnu.questionbank.service;

import com.tnu.questionbank.entity.CourseKnowledge;
import com.tnu.questionbank.mapper.KnowledgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KnowledgeService {

    @Autowired
    private KnowledgeMapper knowledgeMapper;

    // 查询所有知识点（支持模糊搜索）
    public List<CourseKnowledge> findAll(String name) {
        return knowledgeMapper.findAll(name);
    }

    // 根据ID查询
    public CourseKnowledge findById(Integer id) {
        return knowledgeMapper.findById(id);
    }

    // 新增知识点
    public void add(CourseKnowledge knowledge) {
        // 可以在这里添加业务校验，比如检查知识点名称是否重复
        knowledgeMapper.insert(knowledge);
    }

    // 修改知识点
    public void update(CourseKnowledge knowledge) {
        knowledgeMapper.update(knowledge);
    }

    // 删除知识点
    public void delete(Integer id) {
        // 注意：实际项目中应该检查是否有题目关联了该知识点
        // 如果有关联，应该禁止删除或提示用户
        knowledgeMapper.deleteById(id);
    }
}