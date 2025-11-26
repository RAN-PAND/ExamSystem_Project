package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.Question;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionMapper {

    // 1. 查询所有题目 (支持模糊查询)，按 ID 正序排列，支持按题型筛选
    @Select("<script>" +
            "SELECT * FROM question_bank " +
            "<where>" +
            "<if test='content != null and content != \"\"'> AND content LIKE concat('%', #{content}, '%') </if>" +
            "<if test='knowledgeId != null'> AND knowledge_id = #{knowledgeId} </if>" +
            "<if test='difficultyLevel != null'> AND difficulty_level = #{difficultyLevel} </if>" +
            "<if test='questionType != null'> AND question_type = #{questionType} </if>" +
            "</where>" +
            "ORDER BY id ASC" +
            "</script>")
    List<Question> findAll(@Param("content") String content,
                           @Param("knowledgeId") Integer knowledgeId,
                           @Param("difficultyLevel") Integer difficultyLevel,
                           @Param("questionType") Integer questionType);

    // 2. 新增题目（支持题型 question_type，默认为 1-单选题）
    @Insert("INSERT INTO question_bank(" +
            "content, question_type, option_a, option_b, option_c, option_d, answer, " +
            "score_default, knowledge_id, difficulty_level, difficulty_p, discrimination_d" +
            ") VALUES(" +
            "#{content}, IFNULL(#{questionType}, 1), #{optionA}, #{optionB}, #{optionC}, #{optionD}, #{answer}, " +
            "#{scoreDefault}, #{knowledgeId}, #{difficultyLevel}, 0, 0)")
    void insert(Question question);

    // 3. 修改题目
    @Update("UPDATE question_bank SET " +
            "content=#{content}, " +
            "question_type=IFNULL(#{questionType}, 1), " +
            "option_a=#{optionA}, option_b=#{optionB}, option_c=#{optionC}, option_d=#{optionD}, " +
            "answer=#{answer}, difficulty_level=#{difficultyLevel} " +
            "WHERE id = #{id}")
    void update(Question question);

    // 4. 删除题目
    @Delete("DELETE FROM question_bank WHERE id = #{id}")
    void deleteById(Integer id);

    // 批量删除
    @Delete("<script>" +
            "DELETE FROM question_bank WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    void batchDelete(@Param("ids") List<Integer> ids);

    @Select({
            "<script>",
            "SELECT * FROM question_bank WHERE id IN ",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    List<Question> findByIds(@Param("ids") List<Integer> ids);

    // 更新某题目的 difficulty_p
    @Update("UPDATE question_bank SET difficulty_p = #{p} WHERE id = #{id}")
    void updateDifficultyP(@Param("id") Integer id, @Param("p") Float p);

    // 更新某题目的 discrimination_d
    @Update("UPDATE question_bank SET discrimination_d = #{d} WHERE id = #{id}")
    void updateDiscriminationD(@Param("id") Integer id, @Param("d") Float d);
}