package com.example.objectverse.service;

import com.example.objectverse.dto.UmlClassDiagramView;
import com.example.objectverse.dto.UmlRelationView;

import java.util.List;

public interface UmlService {

    String generateClassDiagramText(Long projectId);

    List<UmlClassDiagramView> buildClassDiagramViews(Long projectId);

    List<UmlRelationView> buildRelationViews(Long projectId);
}
