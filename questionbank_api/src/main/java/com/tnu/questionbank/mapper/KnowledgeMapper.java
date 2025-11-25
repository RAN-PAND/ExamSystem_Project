package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.CourseKnowledge;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface KnowledgeMapper {

    // 1. 查询所有知识点（支持按名称模糊查询）
    @Select("<script>" +
            "SELECT * FROM course_knowledge " +
            "<where>" +
            "<if test='name != null and name != \"\"'> AND name LIKE concat('%', #{name}, '%') </if>" +
            "</where>" +
            "ORDER BY id ASC" +
            "</script>")
    List<CourseKnowledge> findAll(String name);

    // 2. 根据ID查询单个知识点
    @Select("SELECT * FROM course_knowledge WHERE id = #{id}")
    CourseKnowledge findById(Integer id);

    // 3. 新增知识点
    @Insert("INSERT INTO course_knowledge(name, target, chapter) " +
            "VALUES(#{name}, #{target}, #{chapter})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CourseKnowledge knowledge);

    // 4. 修改知识点
    @Update("UPDATE course_knowledge SET name=#{name}, target=#{target}, chapter=#{chapter} " +
            "WHERE id = #{id}")
    void update(CourseKnowledge knowledge);

    // 5. 删除知识点
    @Delete("DELETE FROM course_knowledge WHERE id = #{id}")
    void deleteById(Integer id);
}