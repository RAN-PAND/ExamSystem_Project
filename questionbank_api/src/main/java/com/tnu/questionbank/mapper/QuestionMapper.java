package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.Question;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface QuestionMapper {

    // 1. 查询所有题目 (支持模糊查询)
    @Select("<script>" +
            "SELECT * FROM question_bank " +
            "<where>" +
            "<if test='content != null and content != \"\"'> AND content LIKE concat('%', #{content}, '%') </if>" +
            "<if test='knowledgeId != null'> AND knowledge_id = #{knowledgeId} </if>" +
            "<if test='difficultyLevel != null'> AND difficulty_level = #{difficultyLevel} </if>" +
            "</where>" +
            "ORDER BY id DESC" +
            "</script>")
    List<Question> findAll(@Param("content") String content,
                           @Param("knowledgeId") Integer knowledgeId,
                           @Param("difficultyLevel") Integer difficultyLevel);

    // 2. 新增题目
    @Insert("INSERT INTO question_bank(content, option_a, option_b, option_c, option_d, answer, " +
            "score_default, knowledge_id, difficulty_level, difficulty_p, discrimination_d) " +
            "VALUES(#{content}, #{optionA}, #{optionB}, #{optionC}, #{optionD}, #{answer}, " +
            "#{scoreDefault}, #{knowledgeId}, #{difficultyLevel}, 0, 0)")
    void insert(Question question);

    // 3. 修改题目
    @Update("UPDATE question_bank SET content=#{content}, option_a=#{optionA}, option_b=#{optionB}, " +
            "option_c=#{optionC}, option_d=#{optionD}, answer=#{answer}, difficulty_level=#{difficultyLevel} " +
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

    // 更新某题目的 difficulty_p
    @Update("UPDATE question_bank SET difficulty_p = #{p} WHERE id = #{id}")
    void updateDifficultyP(@Param("id") Integer id, @Param("p") Float p);

    // 更新某题目的 discrimination_d
    @Update("UPDATE question_bank SET discrimination_d = #{d} WHERE id = #{id}")
    void updateDiscriminationD(@Param("id") Integer id, @Param("d") Float d);
}