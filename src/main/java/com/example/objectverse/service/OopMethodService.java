package com.example.objectverse.service;

import com.example.objectverse.entity.OopMethod;

import java.util.List;

public interface OopMethodService {

    List<OopMethod> findByClassId(Long classId);

    OopMethod findById(Long id);

    void create(OopMethod method);

    void update(OopMethod method);

    void deleteById(Long id);
}
