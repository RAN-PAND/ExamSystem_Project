package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.ExamRecord;
import com.tnu.questionbank.dto.QuestionStat;
import com.tnu.questionbank.dto.StudentScore;
import com.tnu.questionbank.dto.QuestionGroupStat;
import com.tnu.questionbank.dto.ExamRecordSummaryDTO;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamRecordMapper {

    // 插入一条答题记录
    @Insert("INSERT INTO exam_record(student_id, paper_id, question_id, answer, is_correct, score, submit_time) " +
            "VALUES(#{studentId}, #{paperId}, #{questionId}, #{answer}, #{isCorrect}, #{score}, NOW())")
    void insert(ExamRecord record);

    // 查询某道题目的所有答题记录（如有需要可使用）
    @Select("SELECT * FROM exam_record WHERE question_id = #{questionId}")
    List<ExamRecord> findByQuestionId(Integer questionId);

    // 查询所有题目的统计数据（P值计算用）
    @Select("SELECT question_id AS questionId, COUNT(*) AS totalCount, SUM(is_correct) AS correctCount " +
            "FROM exam_record GROUP BY question_id")
    List<QuestionStat> findQuestionStats();

    // 统计每个学生的总分（D 值计算用）
    @Select("SELECT student_id AS studentId, SUM(score) AS totalScore " +
            "FROM exam_record GROUP BY student_id")
    List<StudentScore> findStudentScores();

    // 针对一批学生ID，统计每道题的作答/答对情况（用于高分组或低分组）
    @Select({
            "<script>",
            "SELECT question_id AS questionId,",
            "       COUNT(*) AS totalCount,",
            "       SUM(is_correct) AS correctCount",
            "FROM exam_record",
            "WHERE student_id IN",
            "  <foreach collection='studentIds' item='sid' open='(' separator=',' close=')'>",
            "    #{sid}",
            "  </foreach>",
            "GROUP BY question_id",
            "</script>"
    })
    List<QuestionGroupStat> findQuestionGroupStats(@Param("studentIds") List<Integer> studentIds);

    // 按学生维度汇总考试记录
    @Select({
            "SELECT",
            "  r.student_id AS studentId,",
            "  r.paper_id   AS paperId,",
            "  p.name       AS paperName,",
            "  SUM(r.score) AS totalScore,",
            "  MAX(r.submit_time) AS submitTime",
            "FROM exam_record r",
            "LEFT JOIN exam_paper p ON r.paper_id = p.id",
            "WHERE r.student_id = #{studentId}",
            "GROUP BY r.student_id, r.paper_id, p.name",
            "ORDER BY submitTime DESC"
    })
    List<ExamRecordSummaryDTO> findSummaryByStudentId(@Param("studentId") Integer studentId);
}