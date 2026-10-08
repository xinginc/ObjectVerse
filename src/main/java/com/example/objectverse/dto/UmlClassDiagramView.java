package com.example.objectverse.dto;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import lombok.Data;

import java.util.List;

@Data
public class UmlClassDiagramView {

    private OopClass oopClass;

    private List<OopAttribute> attributes;

    private List<OopMethod> methods;
}
