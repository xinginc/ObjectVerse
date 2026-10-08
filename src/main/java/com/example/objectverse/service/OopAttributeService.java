package com.example.objectverse.service;

import com.example.objectverse.entity.OopAttribute;

import java.util.List;

public interface OopAttributeService {

    List<OopAttribute> findByClassId(Long classId);

    OopAttribute findById(Long id);

    void create(OopAttribute attribute);

    void update(OopAttribute attribute);

    void deleteById(Long id);
}
