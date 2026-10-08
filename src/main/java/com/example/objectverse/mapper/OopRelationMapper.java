package com.example.objectverse.mapper;

import com.example.objectverse.entity.OopRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OopRelationMapper {

    List<OopRelation> findByProjectId(@Param("projectId") Long projectId);

    OopRelation findById(@Param("id") Long id);

    void insert(OopRelation relation);

    void update(OopRelation relation);

    void deleteById(@Param("id") Long id);
}
