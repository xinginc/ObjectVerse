package com.example.objectverse.service;

import com.example.objectverse.entity.CodeFile;

import java.util.List;

public interface CodeGenerateService {

    String generateClassCode(Long projectId, Long classId, String strategyType);

    List<CodeFile> findGeneratedFiles(Long projectId);

    CodeFile findCodeFileById(Long id);
}
