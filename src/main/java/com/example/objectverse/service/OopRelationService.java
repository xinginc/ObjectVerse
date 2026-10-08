package com.example.objectverse.service;

import com.example.objectverse.entity.OopRelation;

import java.util.List;

public interface OopRelationService {

    List<OopRelation> findByProjectId(Long projectId);

    OopRelation findById(Long id);

    void create(OopRelation relation);

    void update(OopRelation relation);

    void deleteById(Long id);
}
