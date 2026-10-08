package com.example.objectverse.service;

import com.example.objectverse.entity.Project;

import java.util.List;

public interface ProjectService {

    List<Project> findAll();

    Project findById(Long id);

    void create(Project project);

    void update(Project project);

    void updateVisibility(Long id, String visibility);

    void deleteById(Long id);
}
