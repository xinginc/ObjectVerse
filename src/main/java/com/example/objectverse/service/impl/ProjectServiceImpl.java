package com.example.objectverse.service.impl;

import com.example.objectverse.entity.Project;
import com.example.objectverse.mapper.ProjectMapper;
import com.example.objectverse.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private static final String DEFAULT_STATUS = "DRAFT";
    private static final String DEFAULT_VISIBILITY = "PRIVATE";
    private static final String PUBLIC_VISIBILITY = "PUBLIC";

    private final ProjectMapper projectMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @Override
    public List<Project> findAll() {
        return projectMapper.findAll();
    }

    @Override
    public Project findById(Long id) {
        if (id == null) {
            return null;
        }
        return projectMapper.findById(id);
    }

    @Override
    public void create(Project project) {
        LocalDateTime now = LocalDateTime.now();
        if (project.getUserId() == null) {
            throw new IllegalArgumentException("创建项目需要先登录");
        }
        project.setStatus(resolveStatus(project.getStatus()));
        project.setVisibility(resolveVisibility(project.getVisibility()));
        project.setCreateTime(now);
        project.setUpdateTime(now);
        project.setDeleted(0);
        projectMapper.insert(project);
    }

    @Override
    public void update(Project project) {
        project.setStatus(resolveStatus(project.getStatus()));
        project.setVisibility(resolveVisibility(project.getVisibility()));
        project.setUpdateTime(LocalDateTime.now());
        projectMapper.update(project);
    }

    @Override
    public void updateVisibility(Long id, String visibility) {
        if (id != null) {
            projectMapper.updateVisibility(id, resolveVisibility(visibility));
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id != null) {
            projectMapper.deleteById(id);
        }
    }

    private String resolveStatus(String status) {
        if (StringUtils.hasText(status)) {
            return status;
        }
        return DEFAULT_STATUS;
    }

    private String resolveVisibility(String visibility) {
        if (PUBLIC_VISIBILITY.equals(visibility)) {
            return PUBLIC_VISIBILITY;
        }
        return DEFAULT_VISIBILITY;
    }
}
