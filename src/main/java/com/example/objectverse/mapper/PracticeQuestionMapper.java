package com.example.objectverse.mapper;

import com.example.objectverse.entity.PracticeQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PracticeQuestionMapper {

    List<PracticeQuestion> findByUserId(@Param("userId") Long userId);

    int insert(PracticeQuestion question);

    int deleteByUserId(@Param("userId") Long userId);
}
