package com.example.objectverse.service.impl;

import com.example.objectverse.entity.OopClass;
import com.example.objectverse.mapper.OopClassMapper;
import com.example.objectverse.observer.ProjectModelSubject;
import com.example.objectverse.service.OopClassService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OopClassServiceImpl implements OopClassService {

    private static final String DEFAULT_VISIBILITY = "PUBLIC";

    private final OopClassMapper oopClassMapper;
    private final ProjectModelSubject projectModelSubject;

    public OopClassServiceImpl(OopClassMapper oopClassMapper, ProjectModelSubject projectModelSubject) {
        this.oopClassMapper = oopClassMapper;
        this.projectModelSubject = projectModelSubject;
    }

    @Override
    public List<OopClass> findByProjectId(Long projectId) {
        return oopClassMapper.findByProjectId(projectId);
    }

    @Override
    public OopClass findById(Long id) {
        if (id == null) {
            return null;
        }
        return oopClassMapper.findById(id);
    }

    @Override
    public void create(OopClass oopClass) {
        LocalDateTime now = LocalDateTime.now();
        oopClass.setVisibility(resolveVisibility(oopClass.getVisibility()));
        oopClass.setIsAbstract(resolveAbstractFlag(oopClass.getIsAbstract()));
        oopClass.setCreateTime(now);
        oopClass.setUpdateTime(now);
        oopClass.setDeleted(0);
        oopClassMapper.insert(oopClass);
        projectModelSubject.notifyObservers(oopClass.getProjectId(), "OOP_CLASS_CREATED");
    }

    @Override
    public void update(OopClass oopClass) {
        oopClass.setVisibility(resolveVisibility(oopClass.getVisibility()));
        oopClass.setIsAbstract(resolveAbstractFlag(oopClass.getIsAbstract()));
        oopClass.setUpdateTime(LocalDateTime.now());
        oopClassMapper.update(oopClass);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            oopClassMapper.deleteById(id);
        }
    }

    private String resolveVisibility(String visibility) {
        if (StringUtils.hasText(visibility)) {
            return visibility;
        }
        return DEFAULT_VISIBILITY;
    }

    private Boolean resolveAbstractFlag(Boolean isAbstract) {
        return Boolean.TRUE.equals(isAbstract);
    }
}
