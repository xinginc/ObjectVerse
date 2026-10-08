package com.example.objectverse.mapper;

import com.example.objectverse.entity.OopMethod;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OopMethodMapper {

    List<OopMethod> findByClassId(@Param("classId") Long classId);

    OopMethod findById(@Param("id") Long id);

    void insert(OopMethod method);

    void update(OopMethod method);

    void deleteById(@Param("id") Long id);
}
