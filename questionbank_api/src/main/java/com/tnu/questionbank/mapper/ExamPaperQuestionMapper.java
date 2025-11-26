package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamPaperQuestionMapper {

    @Insert("INSERT INTO exam_paper_question(paper_id, question_id, score, order_no) " +
            "VALUES(#{paperId}, #{questionId}, #{score}, #{orderNo})")
    void insert(ExamPaperQuestion pq);

    // 删除某试卷下的所有题目（用于重新配置题目）
    @Delete("DELETE FROM exam_paper_question WHERE paper_id = #{paperId}")
    void deleteByPaperId(Integer paperId);

    // 查询某试卷下的所有题目关联
    @Select("SELECT * FROM exam_paper_question WHERE paper_id = #{paperId} ORDER BY order_no ASC")
    List<ExamPaperQuestion> findByPaperId(Integer paperId);
}
