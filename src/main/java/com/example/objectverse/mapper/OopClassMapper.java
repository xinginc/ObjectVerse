package com.example.objectverse.mapper;

import com.example.objectverse.entity.OopClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OopClassMapper {

    List<OopClass> findByProjectId(@Param("projectId") Long projectId);

    OopClass findById(@Param("id") Long id);

    void insert(OopClass oopClass);

    void update(OopClass oopClass);

    void deleteById(@Param("id") Long id);
}
