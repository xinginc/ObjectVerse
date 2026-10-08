package com.example.objectverse.service.impl;

import com.example.objectverse.dto.UmlClassDiagramView;
import com.example.objectverse.dto.UmlRelationView;
import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import com.example.objectverse.entity.OopRelation;
import com.example.objectverse.service.OopAttributeService;
import com.example.objectverse.service.OopClassService;
import com.example.objectverse.service.OopMethodService;
import com.example.objectverse.service.OopRelationService;
import com.example.objectverse.service.UmlService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UmlServiceImpl implements UmlService {

    private final OopClassService oopClassService;
    private final OopAttributeService oopAttributeService;
    private final OopMethodService oopMethodService;
    private final OopRelationService oopRelationService;

    public UmlServiceImpl(
            OopClassService oopClassService,
            OopAttributeService oopAttributeService,
            OopMethodService oopMethodService,
            OopRelationService oopRelationService
    ) {
        this.oopClassService = oopClassService;
        this.oopAttributeService = oopAttributeService;
        this.oopMethodService = oopMethodService;
        this.oopRelationService = oopRelationService;
    }

    @Override
    public String generateClassDiagramText(Long projectId) {
        StringBuilder text = new StringBuilder();
        text.append("classDiagram\n");
        List<OopClass> classes = oopClassService.findByProjectId(projectId);
        for (OopClass oopClass : classes) {
            appendClassBlock(text, oopClass);
        }
        Map<Long, OopClass> classMap = toClassMap(classes);
        appendRelations(text, oopRelationService.findByProjectId(projectId), classMap);
        return text.toString();
    }

    @Override
    public List<UmlClassDiagramView> buildClassDiagramViews(Long projectId) {
        List<UmlClassDiagramView> views = new ArrayList<>();
        List<OopClass> classes = oopClassService.findByProjectId(projectId);
        for (OopClass oopClass : classes) {
            UmlClassDiagramView view = new UmlClassDiagramView();
            view.setOopClass(oopClass);
            view.setAttributes(oopAttributeService.findByClassId(oopClass.getId()));
            view.setMethods(oopMethodService.findByClassId(oopClass.getId()));
            views.add(view);
        }
        return views;
    }

    @Override
    public List<UmlRelationView> buildRelationViews(Long projectId) {
        Map<Long, OopClass> classMap = toClassMap(oopClassService.findByProjectId(projectId));
        List<UmlRelationView> views = new ArrayList<>();
        for (OopRelation relation : oopRelationService.findByProjectId(projectId)) {
            UmlRelationView view = new UmlRelationView();
            view.setSourceClassName(resolveClassName(relation.getSourceId(), classMap));
            view.setTargetClassName(resolveClassName(relation.getTargetId(), classMap));
            view.setRelationType(resolve(relation.getRelationType(), "ASSOCIATION"));
            view.setDescription(relation.getDescription());
            views.add(view);
        }
        return views;
    }

    private void appendClassBlock(StringBuilder text, OopClass oopClass) {
        String className = toMermaidIdentifier(oopClass.getClassName(), "Class" + oopClass.getId());
        List<OopAttribute> attributes = oopAttributeService.findByClassId(oopClass.getId());
        List<OopMethod> methods = oopMethodService.findByClassId(oopClass.getId());
        if (attributes.isEmpty() && methods.isEmpty() && !Boolean.TRUE.equals(oopClass.getIsAbstract())) {
            text.append("    class ").append(className).append("\n\n");
            return;
        }
        if (Boolean.TRUE.equals(oopClass.getIsAbstract())) {
            text.append("    class ").append(className).append(" {\n");
            text.append("        <<abstract>>\n");
        } else {
            text.append("    class ").append(className).append(" {\n");
        }
        for (OopAttribute attribute : attributes) {
            text.append("        ")
                    .append(toUmlAttributeVisibility(attribute.getVisibility()))
                    .append(toMermaidMember(resolve(attribute.getAttributeType(), "String"), "String"))
                    .append(" ")
                    .append(toMermaidMember(attribute.getAttributeName(), "attribute"))
                    .append("\n");
        }
        if (!attributes.isEmpty()) {
            text.append("\n");
        }
        for (OopMethod method : methods) {
            text.append("        ")
                    .append(toUmlMethodVisibility(method.getVisibility()))
                    .append(toMermaidMember(method.getMethodName(), "method"))
                    .append("(")
                    .append(toMermaidParameters(resolve(method.getParameters(), "")))
                    .append(") ")
                    .append(toMermaidMember(resolve(method.getReturnType(), "void"), "void"))
                    .append("\n");
        }
        text.append("    }\n\n");
    }

    private void appendRelations(StringBuilder text, List<OopRelation> relations, Map<Long, OopClass> classMap) {
        for (OopRelation relation : relations) {
            String sourceName = toMermaidIdentifier(resolveClassName(relation.getSourceId(), classMap), "Source" + relation.getSourceId());
            String targetName = toMermaidIdentifier(resolveClassName(relation.getTargetId(), classMap), "Target" + relation.getTargetId());
            text.append("    ").append(toMermaidRelation(sourceName, targetName, relation.getRelationType()));
            if (StringUtils.hasText(relation.getDescription())) {
                text.append(" : ").append(toMermaidRelationLabel(relation.getDescription()));
            }
            text.append("\n");
        }
    }

    private Map<Long, OopClass> toClassMap(List<OopClass> classes) {
        return classes.stream().collect(Collectors.toMap(OopClass::getId, Function.identity()));
    }

    private String resolveClassName(Long classId, Map<Long, OopClass> classMap) {
        OopClass oopClass = classMap.get(classId);
        if (oopClass != null && StringUtils.hasText(oopClass.getClassName())) {
            return oopClass.getClassName();
        }
        return "Class#" + classId;
    }

    private String resolve(String value, String fallback) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return fallback;
    }

    private String toMermaidRelation(String sourceName, String targetName, String relationType) {
        String type = resolve(relationType, "ASSOCIATION").toUpperCase();
        if ("INHERITANCE".equals(type)) {
            return targetName + " <|-- " + sourceName;
        }
        if ("IMPLEMENTATION".equals(type) || "REALIZATION".equals(type)) {
            return targetName + " <|.. " + sourceName;
        }
        if ("AGGREGATION".equals(type)) {
            return sourceName + " o-- " + targetName;
        }
        if ("COMPOSITION".equals(type)) {
            return sourceName + " *-- " + targetName;
        }
        if ("DEPENDENCY".equals(type)) {
            return sourceName + " ..> " + targetName;
        }
        return sourceName + " --> " + targetName;
    }

    private String toMermaidIdentifier(String value, String fallback) {
        String resolved = resolve(value, fallback).replaceAll("[^A-Za-z0-9_]", "_");
        if (resolved.isBlank()) {
            resolved = fallback;
        }
        if (Character.isDigit(resolved.charAt(0))) {
            resolved = "_" + resolved;
        }
        return resolved;
    }

    private String toMermaidMember(String value, String fallback) {
        return resolve(value, fallback)
                .replace("{", "(")
                .replace("}", ")")
                .replace("[", "(")
                .replace("]", ")")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }

    private String toMermaidParameters(String value) {
        String resolved = toMermaidMember(value, "");
        if (!StringUtils.hasText(resolved)) {
            return "";
        }
        String[] parameters = resolved.split(",");
        List<String> types = new ArrayList<>();
        for (String parameter : parameters) {
            String normalized = parameter.trim().replace(":", " ");
            if (!StringUtils.hasText(normalized)) {
                continue;
            }
            String[] parts = normalized.split("\\s+");
            types.add(parts[0]);
        }
        return String.join(", ", types);
    }

    private String toMermaidRelationLabel(String value) {
        return value.replace("\n", " ")
                .replace("\r", " ")
                .replace(":", " -")
                .trim();
    }

    private String toUmlAttributeVisibility(String visibility) {
        if ("PUBLIC".equalsIgnoreCase(visibility)) {
            return "+";
        }
        if ("PROTECTED".equalsIgnoreCase(visibility)) {
            return "#";
        }
        if ("PACKAGE".equalsIgnoreCase(visibility)
                || "DEFAULT".equalsIgnoreCase(visibility)
                || "PACKAGE_PRIVATE".equalsIgnoreCase(visibility)) {
            return "~";
        }
        return "-";
    }

    private String toUmlMethodVisibility(String visibility) {
        if ("PRIVATE".equalsIgnoreCase(visibility)) {
            return "-";
        }
        if ("PROTECTED".equalsIgnoreCase(visibility)) {
            return "#";
        }
        if ("PACKAGE".equalsIgnoreCase(visibility)
                || "DEFAULT".equalsIgnoreCase(visibility)
                || "PACKAGE_PRIVATE".equalsIgnoreCase(visibility)) {
            return "~";
        }
        return "+";
    }
}
