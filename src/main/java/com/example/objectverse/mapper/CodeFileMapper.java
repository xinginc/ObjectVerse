package com.example.objectverse.mapper;

import com.example.objectverse.entity.CodeFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodeFileMapper {

    void insert(CodeFile codeFile);

    List<CodeFile> findByProjectId(@Param("projectId") Long projectId);

    CodeFile findById(@Param("id") Long id);
}
