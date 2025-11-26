package com.tnu.questionbank.service;

import com.tnu.questionbank.dto.ExamPaperViewDTO;
import com.tnu.questionbank.dto.ExamQuestionViewDTO;
import com.tnu.questionbank.dto.ExamSubmitDTO;
import com.tnu.questionbank.dto.ExamSubmitResultDTO;
import com.tnu.questionbank.entity.ExamPaper;
import com.tnu.questionbank.entity.ExamPaperQuestion;
import com.tnu.questionbank.entity.ExamRecord;
import com.tnu.questionbank.entity.Question;
import com.tnu.questionbank.mapper.ExamPaperMapper;
import com.tnu.questionbank.mapper.ExamPaperQuestionMapper;
import com.tnu.questionbank.mapper.ExamRecordMapper;
import com.tnu.questionbank.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamService {

    @Autowired
    private ExamPaperMapper examPaperMapper;

    @Autowired
    private ExamPaperQuestionMapper examPaperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    public ExamPaperViewDTO getExamPaperView(Integer paperId) {
        // 1. 查试卷基本信息
        ExamPaper paper = examPaperMapper.findById(paperId);
        if (paper == null) {
            return null;
        }

        ExamPaperViewDTO dto = new ExamPaperViewDTO();
        dto.setPaperId(paper.getId());
        dto.setPaperName(paper.getName());
        dto.setDescription(paper.getDescription());
        dto.setTotalScore(paper.getTotalScore());
        dto.setQuestionCount(paper.getQuestionCount());

        // 2. 查试卷题目关联
        List<ExamPaperQuestion> pqList = examPaperQuestionMapper.findByPaperId(paperId);
        if (pqList == null || pqList.isEmpty()) {
            dto.setQuestions(new ArrayList<>());
            return dto;
        }

        // 3. 批量查题目详情
        List<Integer> ids = pqList.stream().map(ExamPaperQuestion::getQuestionId).toList();
        List<Question> questions = questionMapper.findByIds(ids);
        Map<Integer, Question> qMap = new HashMap<>();
        for (Question q : questions) {
            qMap.put(q.getId(), q);
        }

        // 4. 组装考试视图（不带正确答案）
        List<ExamQuestionViewDTO> viewList = new ArrayList<>();
        for (ExamPaperQuestion pq : pqList) {
            Question q = qMap.get(pq.getQuestionId());
            if (q == null) continue;
            ExamQuestionViewDTO v = new ExamQuestionViewDTO();
            v.setId(q.getId());
            v.setContent(q.getContent());
            v.setOptionA(q.getOptionA());
            v.setOptionB(q.getOptionB());
            v.setOptionC(q.getOptionC());
            v.setOptionD(q.getOptionD());
            v.setQuestionType(q.getQuestionType());
            v.setScore(pq.getScore());      // 以试卷中配置的分值为准
            v.setOrderNo(pq.getOrderNo());
            viewList.add(v);
        }

        dto.setQuestions(viewList);
        return dto;
    }

    @Transactional
    public ExamSubmitResultDTO submitExam(ExamSubmitDTO submitDTO) {
        Integer studentId = submitDTO.getStudentId();
        Integer paperId = submitDTO.getPaperId();

        // 1. 构建 questionId -> studentAnswer 映射
        Map<Integer, String> answerMap = new HashMap<>();
        for (ExamSubmitDTO.AnswerItem item : submitDTO.getAnswers()) {
            answerMap.put(item.getQuestionId(), item.getAnswer());
        }

        // 2. 查试卷题目关联
        List<ExamPaperQuestion> pqList = examPaperQuestionMapper.findByPaperId(paperId);
        if (pqList == null || pqList.isEmpty()) {
            return null;
        }

        // 3. 批量查题目详情
        List<Integer> ids = pqList.stream().map(ExamPaperQuestion::getQuestionId).toList();
        List<Question> questions = questionMapper.findByIds(ids);
        Map<Integer, Question> qMap = new HashMap<>();
        for (Question q : questions) {
            qMap.put(q.getId(), q);
        }

        // 4. 判分 & 写入 exam_record
        int totalScore = 0;
        List<ExamSubmitResultDTO.QuestionResult> detailList = new ArrayList<>();

        for (ExamPaperQuestion pq : pqList) {
            Integer qid = pq.getQuestionId();
            Question q = qMap.get(qid);
            if (q == null) continue;

            String studentAnswer = answerMap.get(qid);
            String correctAnswer = q.getAnswer();
            int score = 0;
            boolean correct = false;

            Integer qType = q.getQuestionType();
            int questionType = (qType == null ? 1 : qType); // 默认按单选处理

            // 按题型判分
            if (questionType == 1 || questionType == 2) {
                // 单选 / 多选：不区分大小写直接比较字符串
                if (studentAnswer != null && correctAnswer != null
                        && studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim())) {
                    score = pq.getScore();
                    correct = true;
                }
            } else if (questionType == 3) {
                // 判断题：使用 T/F 编码，统一大小写
                if (studentAnswer != null && correctAnswer != null) {
                    String stu = studentAnswer.trim().toUpperCase();
                    String std = correctAnswer.trim().toUpperCase();
                    if (stu.equals("T") || stu.equals("F")) {
                        if (stu.equals(std)) {
                            score = pq.getScore();
                            correct = true;
                        }
                    }
                }
            } else if (questionType == 4) {
                // 填空题：去掉首尾空格并忽略大小写
                if (studentAnswer != null && correctAnswer != null) {
                    String stu = studentAnswer.trim();
                    String std = correctAnswer.trim();
                    if (!stu.isEmpty() && stu.equalsIgnoreCase(std)) {
                        score = pq.getScore();
                        correct = true;
                    }
                }
            } else if (questionType == 5) {
                // 简答题：当前阶段不做自动判分，统一记 0 分、错误
                score = 0;
                correct = false;
            }

            totalScore += score;

            // 写入 exam_record
            ExamRecord record = new ExamRecord();
            record.setStudentId(studentId);
            record.setPaperId(paperId);
            record.setQuestionId(qid);
            record.setAnswer(studentAnswer);
            record.setIsCorrect(correct ? 1 : 0);
            record.setScore(score);
            // submit_time 由插入SQL中 NOW() 填充
            examRecordMapper.insert(record);

            // 组装返回明细
            ExamSubmitResultDTO.QuestionResult qr = new ExamSubmitResultDTO.QuestionResult();
            qr.setQuestionId(qid);
            qr.setStudentAnswer(studentAnswer);
            qr.setCorrectAnswer(correctAnswer);
            qr.setScore(score);
            qr.setCorrect(correct);
            detailList.add(qr);
        }

        // 5. 组装返回结果
        ExamSubmitResultDTO result = new ExamSubmitResultDTO();
        result.setPaperId(paperId);
        result.setStudentId(studentId);
        result.setTotalScore(totalScore);
        result.setDetails(detailList);

        return result;
    }
}