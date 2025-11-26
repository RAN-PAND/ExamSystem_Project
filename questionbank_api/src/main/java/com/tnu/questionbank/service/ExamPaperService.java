package com.tnu.questionbank.service;

import com.tnu.questionbank.entity.ExamPaper;
import com.tnu.questionbank.entity.ExamPaperQuestion;
import com.tnu.questionbank.entity.Question;
import com.tnu.questionbank.dto.ExamPaperDetailDTO;
import com.tnu.questionbank.mapper.ExamPaperMapper;
import com.tnu.questionbank.mapper.ExamPaperQuestionMapper;
import com.tnu.questionbank.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class ExamPaperService {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    public List<ExamPaper> listAll() {
        return examPaperMapper.findAll();
    }

    @Transactional
    public void savePaperWithQuestions(ExamPaper paper, List<ExamPaperQuestion> questionList) {
        // 1. 计算总分和题目数量
        int totalScore = 0;
        if (questionList != null) {
            totalScore = questionList.stream()
                    .mapToInt(q -> q.getScore() == null ? 0 : q.getScore())
                    .sum();
            paper.setQuestionCount(questionList.size());
        } else {
            paper.setQuestionCount(0);
        }
        paper.setTotalScore(totalScore);

        // 2. 新增或更新试卷
        if (paper.getId() == null) {
            examPaperMapper.insert(paper);
        } else {
            examPaperMapper.update(paper);
            // 先清空旧的题目关联
            examPaperQuestionMapper.deleteByPaperId(paper.getId());
        }

        // 3. 重新插入题目关联
        if (questionList != null) {
            int order = 1;
            for (ExamPaperQuestion pq : questionList) {
                pq.setPaperId(paper.getId());
                if (pq.getOrderNo() == null) {
                    pq.setOrderNo(order++);
                }
                examPaperQuestionMapper.insert(pq);
            }
        }
    }

    @Transactional
    public void deletePaper(Integer id) {
        examPaperQuestionMapper.deleteByPaperId(id);
        examPaperMapper.deleteById(id);
    }

    public ExamPaperDetailDTO getPaperDetail(Integer id) {
        ExamPaper paper = examPaperMapper.findById(id);
        if (paper == null) {
            return null;
        }
        ExamPaperDetailDTO dto = new ExamPaperDetailDTO();
        dto.setId(paper.getId());
        dto.setName(paper.getName());
        dto.setDescription(paper.getDescription());
        dto.setTotalScore(paper.getTotalScore());
        dto.setQuestionCount(paper.getQuestionCount());

        List<ExamPaperQuestion> pqList = examPaperQuestionMapper.findByPaperId(id);
        if (pqList != null && !pqList.isEmpty()) {
            List<Integer> questionIds = new ArrayList<>();
            for (ExamPaperQuestion pq : pqList) {
                questionIds.add(pq.getQuestionId());
            }

            List<Question> questions = questionMapper.findByIds(questionIds);
            Map<Integer, Question> questionMap = new HashMap<>();
            for (Question q : questions) {
                questionMap.put(q.getId(), q);
            }

            List<Question> orderedQuestions = new ArrayList<>();
            for (ExamPaperQuestion pq : pqList) {
                Question q = questionMap.get(pq.getQuestionId());
                if (q != null) {
                    orderedQuestions.add(q);
                }
            }
            dto.setQuestions(orderedQuestions);
        }

        return dto;
    }
}