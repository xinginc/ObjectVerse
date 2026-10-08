package com.example.objectverse.service.impl;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.mapper.OopAttributeMapper;
import com.example.objectverse.observer.ProjectModelSubject;
import com.example.objectverse.service.OopAttributeService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OopAttributeServiceImpl implements OopAttributeService {

    private static final String DEFAULT_VISIBILITY = "PRIVATE";
    private static final String DEFAULT_ATTRIBUTE_TYPE = "String";

    private final OopAttributeMapper oopAttributeMapper;
    private final ProjectModelSubject projectModelSubject;

    public OopAttributeServiceImpl(OopAttributeMapper oopAttributeMapper, ProjectModelSubject projectModelSubject) {
        this.oopAttributeMapper = oopAttributeMapper;
        this.projectModelSubject = projectModelSubject;
    }

    @Override
    public List<OopAttribute> findByClassId(Long classId) {
        return oopAttributeMapper.findByClassId(classId);
    }

    @Override
    public OopAttribute findById(Long id) {
        if (id == null) {
            return null;
        }
        return oopAttributeMapper.findById(id);
    }

    @Override
    public void create(OopAttribute attribute) {
        LocalDateTime now = LocalDateTime.now();
        attribute.setVisibility(resolveVisibility(attribute.getVisibility()));
        attribute.setAttributeType(resolveAttributeType(attribute.getAttributeType()));
        attribute.setCreateTime(now);
        attribute.setUpdateTime(now);
        attribute.setDeleted(0);
        oopAttributeMapper.insert(attribute);
        projectModelSubject.notifyObservers(attribute.getProjectId(), "OOP_ATTRIBUTE_CREATED");
    }

    @Override
    public void update(OopAttribute attribute) {
        attribute.setVisibility(resolveVisibility(attribute.getVisibility()));
        attribute.setAttributeType(resolveAttributeType(attribute.getAttributeType()));
        attribute.setUpdateTime(LocalDateTime.now());
        oopAttributeMapper.update(attribute);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            oopAttributeMapper.deleteById(id);
        }
    }

    private String resolveVisibility(String visibility) {
        if (StringUtils.hasText(visibility)) {
            return visibility;
        }
        return DEFAULT_VISIBILITY;
    }

    private String resolveAttributeType(String attributeType) {
        if (StringUtils.hasText(attributeType)) {
            return attributeType;
        }
        return DEFAULT_ATTRIBUTE_TYPE;
    }
}
