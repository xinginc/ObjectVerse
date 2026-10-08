package com.example.objectverse.strategy;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;

import java.util.List;

public interface CodeGenerateStrategy {

    String getStrategyName();

    String generate(OopClass oopClass, List<OopAttribute> attributes, List<OopMethod> methods);
}
