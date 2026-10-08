package com.example.objectverse.service.impl;

import com.example.objectverse.entity.OopRelation;
import com.example.objectverse.mapper.OopRelationMapper;
import com.example.objectverse.service.OopRelationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OopRelationServiceImpl implements OopRelationService {

    private static final String DEFAULT_NODE_TYPE = "CLASS";
    private static final String DEFAULT_RELATION_TYPE = "ASSOCIATION";

    private final OopRelationMapper oopRelationMapper;

    public OopRelationServiceImpl(OopRelationMapper oopRelationMapper) {
        this.oopRelationMapper = oopRelationMapper;
    }

    @Override
    public List<OopRelation> findByProjectId(Long projectId) {
        return oopRelationMapper.findByProjectId(projectId);
    }

    @Override
    public OopRelation findById(Long id) {
        if (id == null) {
            return null;
        }
        return oopRelationMapper.findById(id);
    }

    @Override
    public void create(OopRelation relation) {
        LocalDateTime now = LocalDateTime.now();
        relation.setSourceType(resolveNodeType(relation.getSourceType()));
        relation.setTargetType(resolveNodeType(relation.getTargetType()));
        relation.setRelationType(resolveRelationType(relation.getRelationType()));
        relation.setCreateTime(now);
        relation.setUpdateTime(now);
        relation.setDeleted(0);
        oopRelationMapper.insert(relation);
    }

    @Override
    public void update(OopRelation relation) {
        relation.setSourceType(resolveNodeType(relation.getSourceType()));
        relation.setTargetType(resolveNodeType(relation.getTargetType()));
        relation.setRelationType(resolveRelationType(relation.getRelationType()));
        relation.setUpdateTime(LocalDateTime.now());
        oopRelationMapper.update(relation);
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            oopRelationMapper.deleteById(id);
        }
    }

    private String resolveNodeType(String nodeType) {
        if (StringUtils.hasText(nodeType)) {
            return nodeType;
        }
        return DEFAULT_NODE_TYPE;
    }

    private String resolveRelationType(String relationType) {
        if (StringUtils.hasText(relationType)) {
            return relationType;
        }
        return DEFAULT_RELATION_TYPE;
    }
}
