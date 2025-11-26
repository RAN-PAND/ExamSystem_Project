package com.tnu.questionbank.service;

import com.alibaba.excel.EasyExcel;
import com.tnu.questionbank.dto.QuestionExcelDTO;
import com.tnu.questionbank.dto.QuestionStat;
import com.tnu.questionbank.dto.StudentScore;
import com.tnu.questionbank.dto.QuestionGroupStat;
import com.tnu.questionbank.mapper.ExamRecordMapper;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tnu.questionbank.entity.Question;
import com.tnu.questionbank.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    // 分页查询，支持按题型筛选
    public PageInfo<Question> findByPage(Integer pageNum, Integer pageSize,
                                         String content, Integer knowledgeId,
                                         Integer difficultyLevel, Integer questionType) {
        PageHelper.startPage(pageNum, pageSize);
        List<Question> list = questionMapper.findAll(content, knowledgeId, difficultyLevel, questionType);
        return new PageInfo<>(list);
    }

    public void add(Question question) {
        questionMapper.insert(question);
    }

    public void update(Question question) {
        questionMapper.update(question);
    }

    public void delete(Integer id) {
        questionMapper.deleteById(id);
    }

    // 批量删除
    public void batchDelete(List<Integer> ids) {
        if (ids != null && !ids.isEmpty()) {
            questionMapper.batchDelete(ids);
        }
    }

    @Transactional  // 导入过程出现异常时，整体回滚
    public void importExcel(MultipartFile file) {
        try {
            // 1. 读取 Excel 中的所有行
            List<QuestionExcelDTO> list = EasyExcel
                    .read(file.getInputStream())
                    .head(QuestionExcelDTO.class)
                    .sheet()
                    .doReadSync();

            // 2. 遍历转换为 Question 实体并插入数据库
            for (QuestionExcelDTO dto : list) {
                Question question = new Question();
                question.setContent(dto.getContent());
                question.setOptionA(dto.getOptionA());
                question.setOptionB(dto.getOptionB());
                question.setOptionC(dto.getOptionC());
                question.setOptionD(dto.getOptionD());
                question.setAnswer(dto.getAnswer());
                question.setDifficultyLevel(dto.getDifficultyLevel());
                question.setKnowledgeId(dto.getKnowledgeId());

                // 动态难度、区分度等字段初始化为 0
                question.setDifficultyP(0f);
                question.setDiscriminationD(0f);
                question.setExposureCount(0);

                questionMapper.insert(question);
            }
        } catch (Exception e) {
            // 抛出异常，触发事务回滚
            throw new RuntimeException("导入题库失败：" + e.getMessage(), e);
        }
    }

    public void exportExcel(HttpServletResponse response) {
        try {
            // 1. 查询所有题目（此处简单导出全部，可根据需要增加筛选条件）
            List<Question> list = questionMapper.findAll(null, null, null, null);

            // 2. 转成 DTO 列表
            List<QuestionExcelDTO> dtoList = new ArrayList<>();
            for (Question q : list) {
                QuestionExcelDTO dto = new QuestionExcelDTO();
                dto.setContent(q.getContent());
                dto.setOptionA(q.getOptionA());
                dto.setOptionB(q.getOptionB());
                dto.setOptionC(q.getOptionC());
                dto.setOptionD(q.getOptionD());
                dto.setAnswer(q.getAnswer());
                dto.setDifficultyLevel(q.getDifficultyLevel());
                dto.setKnowledgeId(q.getKnowledgeId());
                dtoList.add(dto);
            }

            // 3. 设置响应头，告诉浏览器是文件下载
            String fileName = URLEncoder.encode("题库导出.xlsx", "UTF-8");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 4. 使用 EasyExcel 写出
            EasyExcel.write(response.getOutputStream(), QuestionExcelDTO.class)
                    .sheet("题库")
                    .doWrite(dtoList);

        } catch (Exception e) {
            throw new RuntimeException("导出题库失败：" + e.getMessage(), e);
        }
    }

    @Transactional
    public void recalcDifficultyP() {
        List<QuestionStat> stats = examRecordMapper.findQuestionStats();
        for (QuestionStat stat : stats) {
            if (stat.getTotalCount() == null || stat.getTotalCount() == 0) {
                continue;
            }
            float p = stat.getCorrectCount() * 1.0f / stat.getTotalCount();
            questionMapper.updateDifficultyP(stat.getQuestionId(), p);
        }
    }

    @Transactional
    public void recalcDiscriminationD() {
        // 1. 查询每个学生的总分
        List<StudentScore> scores = examRecordMapper.findStudentScores();
        if (scores == null || scores.size() < 2) {
            // 学生太少，无法计算区分度
            return;
        }

        // 2. 按总分从高到低排序
        scores.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));

        int n = scores.size();
        // 3. 计算高/低 27% 组的人数（至少为1，且不超过 n/2）
        int groupSize = (int) Math.round(n * 0.27);
        if (groupSize < 1) {
            groupSize = 1;
        }
        if (groupSize * 2 > n) {
            groupSize = n / 2;
            if (groupSize < 1) {
                groupSize = 1;
            }
        }

        // 4. 取出高分组、低分组的学生ID
        List<Integer> highIds = new ArrayList<>();
        List<Integer> lowIds = new ArrayList<>();

        for (int i = 0; i < groupSize; i++) {
            highIds.add(scores.get(i).getStudentId());
        }
        for (int i = n - groupSize; i < n; i++) {
            lowIds.add(scores.get(i).getStudentId());
        }

        // 5. 分别统计高分组、低分组中每道题的统计信息
        List<QuestionGroupStat> highStats = examRecordMapper.findQuestionGroupStats(highIds);
        List<QuestionGroupStat> lowStats = examRecordMapper.findQuestionGroupStats(lowIds);

        Map<Integer, QuestionGroupStat> highMap = new HashMap<>();
        for (QuestionGroupStat s : highStats) {
            highMap.put(s.getQuestionId(), s);
        }

        Map<Integer, QuestionGroupStat> lowMap = new HashMap<>();
        for (QuestionGroupStat s : lowStats) {
            lowMap.put(s.getQuestionId(), s);
        }

        // 6. 遍历所有出现过的题目ID，计算 D 值
        Set<Integer> allQuestionIds = new HashSet<>();
        allQuestionIds.addAll(highMap.keySet());
        allQuestionIds.addAll(lowMap.keySet());

        for (Integer qid : allQuestionIds) {
            QuestionGroupStat hs = highMap.get(qid);
            QuestionGroupStat ls = lowMap.get(qid);

            if (hs == null || hs.getTotalCount() == null || hs.getTotalCount() == 0) {
                continue; // 高分组没人做这题，无法计算
            }
            if (ls == null || ls.getTotalCount() == null || ls.getTotalCount() == 0) {
                continue; // 低分组没人做这题，无法计算
            }

            float pHigh = hs.getCorrectCount() * 1.0f / hs.getTotalCount();
            float pLow = ls.getCorrectCount() * 1.0f / ls.getTotalCount();
            float d = pHigh - pLow;

            // 7. 写回 question_bank.discrimination_d
            questionMapper.updateDiscriminationD(qid, d);
        }
    }
}