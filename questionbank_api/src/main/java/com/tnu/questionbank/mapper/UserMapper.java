package com.tnu.questionbank.mapper;

import com.tnu.questionbank.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.Map;

@Mapper
public interface UserMapper {

    // WHERE user_num = #{userNum}
    @Select("SELECT * FROM sys_user WHERE user_num = #{userNum}")
    SysUser findByUserNum(String userNum);

    // 参数映射 p_user_num -> #{userNum}
    @Select("CALL proc_register_user(" +
            "#{userNum, mode=IN, jdbcType=VARCHAR}, " +
            "#{password, mode=IN, jdbcType=VARCHAR}, " +
            "#{role, mode=IN, jdbcType=VARCHAR}, " +
            "#{realName, mode=IN, jdbcType=VARCHAR}, " +
            "#{result, mode=OUT, jdbcType=INTEGER})")
    @Options(statementType = StatementType.CALLABLE)
    void registerUser(Map<String, Object> params);
}