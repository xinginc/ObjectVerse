package com.example.objectverse.service.impl;

import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.mapper.OopMethodMapper;
import com.example.objectverse.observer.ProjectModelSubject;
import com.example.objectverse.service.OopMethodService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OopMethodServiceImpl implements OopMethodService {

    private static final String DEFAULT_VISIBILITY = "PUBLIC";
    private static final String DEFAULT_RETURN_TYPE = "void";
    private static final String DEFAULT_PARAMETERS = "";
    private static final String DEFAULT_METHOD_BODY = "// TODO: 实现方法逻辑";

    private final OopMethodMapper oopMethodMapper;
    private final ProjectModelSubject projectModelSubject;

    public OopMethodServiceImpl(OopMethodMapper oopMethodMapper, ProjectModelSubject projectModelSubject) {
        this.oopMethodMapper = oopMethodMapper;
        this.projectModelSubject = projectModelSubject;
    }

    @Override
    public List<OopMethod> findByClassId(Long classId) {
        return oopMethodMapper.findByClassId(classId);
    }

    @Override
    public OopMethod findById(Long id) {
        if (id == null) {
            return null;
        }
        return oopMethodMapper.findById(id);
    }

    @Override
    public void create(OopMethod method) {
        LocalDateTime now = LocalDateTime.now();
        method.setVisibility(resolveVisibility(method.getVisibility()));
        method.setReturnType(resolveReturnType(method.getReturnType()));
        method.setParameters(resolveParameters(method.getParameters()));
        method.setMethodBody(resolveMethodBody(method.getMethodBody()));
        method.setCreateTime(now);
        method.setUpdateTime(now);
        method.setDeleted(0);
        oopMethodMapper.insert(method);
        projectModelSubject.notifyObservers(method.getProjectId(), "OOP_METHOD_CREATED");
    }

    @Override
    public void update(OopMethod method) {
        method.setVisibility(resolveVisibility(method.getVisibility()));
        method.setReturnType(resolveReturnType(method.getReturnType()));
        method.setParameters(resolveParameters(method.getParameters()));
        method.setMethodBody(resolveMethodBody(method.getMethodBody()));
        method.setUpdateTime(LocalDateTime.now());
        oopMethodMapper.update(method);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            oopMethodMapper.deleteById(id);
        }
    }

    private String resolveVisibility(String visibility) {
        if (StringUtils.hasText(visibility)) {
            return visibility;
        }
        return DEFAULT_VISIBILITY;
    }

    private String resolveReturnType(String returnType) {
        if (StringUtils.hasText(returnType)) {
            return returnType;
        }
        return DEFAULT_RETURN_TYPE;
    }

    private String resolveParameters(String parameters) {
        if (StringUtils.hasText(parameters)) {
            return parameters;
        }
        return DEFAULT_PARAMETERS;
    }

    private String resolveMethodBody(String methodBody) {
        if (StringUtils.hasText(methodBody)) {
            return methodBody;
        }
        return DEFAULT_METHOD_BODY;
    }
}
