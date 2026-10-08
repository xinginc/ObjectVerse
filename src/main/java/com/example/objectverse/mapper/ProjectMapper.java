package com.example.objectverse.mapper;

import com.example.objectverse.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {

    List<Project> findAll();

    Project findById(@Param("id") Long id);

    int insert(Project project);

    int update(Project project);

    int updateVisibility(@Param("id") Long id, @Param("visibility") String visibility);

    int deleteById(@Param("id") Long id);
}
