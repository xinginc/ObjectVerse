package com.example.objectverse.mapper;

import com.example.objectverse.entity.OopAttribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OopAttributeMapper {

    List<OopAttribute> findByClassId(@Param("classId") Long classId);

    OopAttribute findById(@Param("id") Long id);

    void insert(OopAttribute attribute);

    void update(OopAttribute attribute);

    void deleteById(@Param("id") Long id);
}
