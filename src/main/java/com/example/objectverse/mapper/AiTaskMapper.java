package com.example.objectverse.mapper;

import com.example.objectverse.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiTaskMapper {

    void insert(AiTask task);

    List<AiTask> findByProjectId(@Param("projectId") Long projectId);
}
