package com.example.objectverse.strategy;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component
public class BasicJavaClassGenerateStrategy implements CodeGenerateStrategy {

    @Override
    public String getStrategyName() {
        return "BASIC";
    }

    @Override
    public String generate(OopClass oopClass, List<OopAttribute> attributes, List<OopMethod> methods) {
        StringBuilder code = new StringBuilder();
        appendPackage(code, oopClass);
        appendImports(code, collectImports(attributes, methods));
        appendClassComment(code, oopClass);
        appendClassHeader(code, oopClass);
        appendFields(code, attributes);
        appendConstructor(code, oopClass);
        appendAccessors(code, attributes);
        appendMethods(code, methods);
        code.append("}\n");
        return code.toString();
    }

    protected void appendPackage(StringBuilder code, OopClass oopClass) {
        if (StringUtils.hasText(oopClass.getPackageName())) {
            code.append("package ").append(oopClass.getPackageName()).append(";\n\n");
        }
    }

    protected void appendImports(StringBuilder code, Set<String> imports) {
        for (String importName : imports) {
            code.append("import ").append(importName).append(";\n");
        }
        if (!imports.isEmpty()) {
            code.append("\n");
        }
    }

    protected void appendClassComment(StringBuilder code, OopClass oopClass) {
        code.append("/**\n")
                .append(" * 类名：").append(oopClass.getClassName()).append("\n")
                .append(" * 说明：").append(resolveText(oopClass.getDescription(), "暂无说明")).append("\n")
                .append(" * 面向对象含义：该类封装对象状态和对象行为。\n")
                .append(" */\n");
    }

    protected void appendClassHeader(StringBuilder code, OopClass oopClass) {
        String classType = Boolean.TRUE.equals(oopClass.getIsAbstract()) ? "abstract class" : "class";
        appendModifier(code, toJavaVisibility(oopClass.getVisibility()));
        code.append(classType).append(" ")
                .append(oopClass.getClassName()).append(" {\n\n");
    }

    protected void appendFields(StringBuilder code, List<OopAttribute> attributes) {
        for (OopAttribute attribute : attributes) {
            appendFieldComment(code, attribute);
            code.append("    ");
            appendModifier(code, toJavaVisibility(attribute.getVisibility()));
            code.append(resolveType(attribute.getAttributeType(), "String"))
                    .append(" ")
                    .append(attribute.getAttributeName());
            if (StringUtils.hasText(attribute.getDefaultValue())) {
                code.append(" = ").append(attribute.getDefaultValue());
            }
            code.append(";\n\n");
        }
    }

    protected void appendFieldComment(StringBuilder code, OopAttribute attribute) {
        code.append("    /** ")
                .append(resolveText(attribute.getDescription(), attribute.getAttributeName()))
                .append(" */\n");
    }

    protected void appendConstructor(StringBuilder code, OopClass oopClass) {
        code.append("    public ").append(oopClass.getClassName()).append("() {\n")
                .append("    }\n\n");
    }

    protected void appendAccessors(StringBuilder code, List<OopAttribute> attributes) {
        for (OopAttribute attribute : attributes) {
            String type = resolveType(attribute.getAttributeType(), "String");
            String name = attribute.getAttributeName();
            String methodSuffix = upperFirst(name);
            code.append("    public ").append(type).append(" get").append(methodSuffix).append("() {\n")
                    .append("        return ").append(name).append(";\n")
                    .append("    }\n\n");
            code.append("    public void set").append(methodSuffix).append("(").append(type).append(" ").append(name).append(") {\n")
                    .append("        this.").append(name).append(" = ").append(name).append(";\n")
                    .append("    }\n\n");
        }
    }

    protected void appendMethods(StringBuilder code, List<OopMethod> methods) {
        for (OopMethod method : methods) {
            appendMethodComment(code, method);
            code.append("    ");
            appendModifier(code, toJavaVisibility(method.getVisibility()));
            code.append(resolveType(method.getReturnType(), "void"))
                    .append(" ")
                    .append(method.getMethodName())
                    .append("(")
                    .append(resolveText(method.getParameters()))
                    .append(") {\n");
            appendMethodBody(code, method);
            code.append("    }\n\n");
        }
    }

    protected void appendMethodComment(StringBuilder code, OopMethod method) {
        code.append("    /**\n")
                .append("     * ").append(resolveText(method.getDescription(), method.getMethodName())).append("。\n");
        for (String parameter : splitParameters(method.getParameters())) {
            String[] parts = parameter.trim().split("\\s+", 2);
            if (parts.length == 2) {
                code.append("     * @param ").append(parts[1]).append(" 参数说明\n");
            }
        }
        String returnType = resolveType(method.getReturnType(), "void");
        if (!"void".equalsIgnoreCase(returnType)) {
            code.append("     * @return 返回结果\n");
        }
        code.append("     */\n");
    }

    protected void appendMethodBody(StringBuilder code, OopMethod method) {
        String body = resolveText(method.getMethodBody());
        if (!StringUtils.hasText(body)) {
            body = defaultMethodBody(resolveType(method.getReturnType(), "void"));
        }
        for (String line : body.split("\\R", -1)) {
            code.append("        ").append(line).append("\n");
        }
    }

    protected String defaultMethodBody(String returnType) {
        String normalized = simpleTypeName(resolveType(returnType, "void"));
        if ("void".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑";
        }
        if ("String".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑\nreturn \"\";";
        }
        if ("Boolean".equals(normalized) || "boolean".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑\nreturn Boolean.FALSE;";
        }
        if ("Integer".equals(normalized) || "int".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑\nreturn 0;";
        }
        if ("Double".equals(normalized) || "double".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑\nreturn 0.0;";
        }
        if ("Long".equals(normalized) || "long".equals(normalized)) {
            return "// TODO: 根据业务规则完善方法逻辑\nreturn 0L;";
        }
        return "// TODO: 根据业务规则完善方法逻辑\nreturn null;";
    }

    protected Set<String> collectImports(List<OopAttribute> attributes, List<OopMethod> methods) {
        Set<String> imports = new TreeSet<>();
        for (OopAttribute attribute : attributes) {
            addImport(imports, attribute.getAttributeType());
        }
        for (OopMethod method : methods) {
            addImport(imports, method.getReturnType());
            for (String parameter : splitParameters(method.getParameters())) {
                addImport(imports, extractParameterType(parameter));
            }
        }
        return imports;
    }

    protected void addImport(Set<String> imports, String type) {
        String normalized = simpleTypeName(type);
        if ("LocalDate".equals(normalized)) {
            imports.add("java.time.LocalDate");
        } else if ("LocalDateTime".equals(normalized)) {
            imports.add("java.time.LocalDateTime");
        } else if ("BigDecimal".equals(normalized)) {
            imports.add("java.math.BigDecimal");
        } else if ("List".equals(normalized)) {
            imports.add("java.util.List");
        } else if ("Map".equals(normalized)) {
            imports.add("java.util.Map");
        }
    }

    protected String simpleTypeName(String type) {
        if (!StringUtils.hasText(type)) {
            return "";
        }
        String normalized = type.trim();
        int genericIndex = normalized.indexOf('<');
        if (genericIndex > 0) {
            normalized = normalized.substring(0, genericIndex);
        }
        int arrayIndex = normalized.indexOf('[');
        if (arrayIndex > 0) {
            normalized = normalized.substring(0, arrayIndex);
        }
        int packageIndex = normalized.lastIndexOf('.');
        if (packageIndex >= 0) {
            normalized = normalized.substring(packageIndex + 1);
        }
        return normalized.trim();
    }

    protected String extractParameterType(String parameter) {
        if (!StringUtils.hasText(parameter)) {
            return "";
        }
        String[] parts = parameter.trim().split("\\s+");
        return parts.length == 0 ? "" : parts[0];
    }

    protected String[] splitParameters(String parameters) {
        if (!StringUtils.hasText(parameters)) {
            return new String[0];
        }
        return parameters.split(",");
    }

    protected void appendModifier(StringBuilder code, String modifier) {
        if (StringUtils.hasText(modifier)) {
            code.append(modifier).append(" ");
        }
    }

    protected String toJavaVisibility(String visibility) {
        if (!StringUtils.hasText(visibility) || "PACKAGE_PRIVATE".equalsIgnoreCase(visibility)) {
            return "";
        }
        return visibility.toLowerCase();
    }

    protected String resolveType(String type, String defaultType) {
        if (StringUtils.hasText(type)) {
            return type;
        }
        return defaultType;
    }

    protected String resolveText(String text) {
        return resolveText(text, "");
    }

    protected String resolveText(String text, String fallback) {
        if (StringUtils.hasText(text)) {
            return text;
        }
        return fallback;
    }

    protected String upperFirst(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
