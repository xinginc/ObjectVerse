package com.example.objectverse.service;

import com.example.objectverse.entity.OopClass;

import java.util.List;

public interface OopClassService {

    List<OopClass> findByProjectId(Long projectId);

    OopClass findById(Long id);

    void create(OopClass oopClass);

    void update(OopClass oopClass);

    void deleteById(Long id);
}
