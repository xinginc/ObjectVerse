package com.example.objectverse.service.impl;

import com.example.objectverse.entity.CodeFile;
import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.mapper.CodeFileMapper;
import com.example.objectverse.service.CodeGenerateService;
import com.example.objectverse.service.OopAttributeService;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopMethodService;
import com.example.objectverse.strategy.CodeGenerateStrategy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CodeGenerateServiceImpl implements CodeGenerateService {

    private static final String DEFAULT_STRATEGY = "ENCAPSULATION";
    private static final String CODE_FILE_TYPE = "JAVA_CLASS";

    private final OopClassService oopClassService;
    private final OopAttributeService oopAttributeService;
    private final OopMethodService oopMethodService;
    private final CodeFileMapper codeFileMapper;
    private final Map<String, CodeGenerateStrategy> strategyMap;

    public CodeGenerateServiceImpl(
            OopClassService oopClassService,
            OopAttributeService oopAttributeService,
            OopMethodService oopMethodService,
            CodeFileMapper codeFileMapper,
            List<CodeGenerateStrategy> strategies
    ) {
        this.oopClassService = oopClassService;
        this.oopAttributeService = oopAttributeService;
        this.oopMethodService = oopMethodService;
        this.codeFileMapper = codeFileMapper;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(CodeGenerateStrategy::getStrategyName, Function.identity()));
    }

    @Override
    public String generateClassCode(Long projectId, Long classId, String strategyType) {
        OopClass oopClass = oopClassService.findById(classId);
        if (oopClass == null) {
            return "";
        }
        List<OopAttribute> attributes = oopAttributeService.findByClassId(classId);
        List<OopMethod> methods = oopMethodService.findByClassId(classId);
        CodeGenerateStrategy strategy = resolveStrategy(strategyType);
        String codeContent = stripLegacyFileHeader(strategy.generate(oopClass, attributes, methods));
        saveCodeFile(projectId, oopClass, codeContent);
        return codeContent;
    }

    @Override
    public List<CodeFile> findGeneratedFiles(Long projectId) {
        return codeFileMapper.findByProjectId(projectId);
    }

    @Override
    public CodeFile findCodeFileById(Long id) {
        if (id == null) {
            return null;
        }
        CodeFile codeFile = codeFileMapper.findById(id);
        if (codeFile != null) {
            codeFile.setCodeContent(stripLegacyFileHeader(codeFile.getCodeContent()));
        }
        return codeFile;
    }

    private CodeGenerateStrategy resolveStrategy(String strategyType) {
        String key = StringUtils.hasText(strategyType) ? strategyType : DEFAULT_STRATEGY;
        CodeGenerateStrategy strategy = strategyMap.get(key);
        if (strategy != null) {
            return strategy;
        }
        return strategyMap.get(DEFAULT_STRATEGY);
    }

    private void saveCodeFile(Long projectId, OopClass oopClass, String codeContent) {
        LocalDateTime now = LocalDateTime.now();
        CodeFile codeFile = new CodeFile();
        codeFile.setProjectId(projectId);
        codeFile.setFileName(oopClass.getClassName() + ".java");
        codeFile.setPackageName(oopClass.getPackageName());
        codeFile.setFileType(CODE_FILE_TYPE);
        codeFile.setCodeContent(codeContent);
        codeFile.setCreateTime(now);
        codeFile.setUpdateTime(now);
        codeFile.setDeleted(0);
        codeFileMapper.insert(codeFile);
    }

    private String stripLegacyFileHeader(String codeContent) {
        if (!StringUtils.hasText(codeContent)) {
            return codeContent;
        }
        return codeContent.replaceFirst(
                "(?s)^\\s*/\\*\\*.*?ObjectVerse\\s*自动生成.*?\\*/\\s*",
                ""
        );
    }
}
