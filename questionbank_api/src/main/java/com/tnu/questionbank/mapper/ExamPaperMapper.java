package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.ExamPaper;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface ExamPaperMapper {

    @Select("SELECT * FROM exam_paper ORDER BY id ASC")
    List<ExamPaper> findAll();

    @Insert("INSERT INTO exam_paper(name, description, total_score, question_count) " +
            "VALUES(#{name}, #{description}, #{totalScore}, #{questionCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ExamPaper paper);

    @Update("UPDATE exam_paper SET name=#{name}, description=#{description}, " +
            "total_score=#{totalScore}, question_count=#{questionCount} WHERE id=#{id}")
    void update(ExamPaper paper);

    @Delete("DELETE FROM exam_paper WHERE id = #{id}")
    void deleteById(Integer id);

    @Select("SELECT * FROM exam_paper WHERE id = #{id}")
    ExamPaper findById(Integer id);
}