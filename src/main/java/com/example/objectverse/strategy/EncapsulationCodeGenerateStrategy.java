package com.example.objectverse.strategy;

import com.example.objectverse.entity.OopAttribute;
import com.example.objectverse.entity.OopClass;
import com.example.objectverse.entity.OopMethod;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EncapsulationCodeGenerateStrategy extends BasicJavaClassGenerateStrategy {

    @Override
    public String getStrategyName() {
        return "ENCAPSULATION";
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

    @Override
    protected void appendFields(StringBuilder code, List<OopAttribute> attributes) {
        for (OopAttribute attribute : attributes) {
            appendFieldComment(code, attribute);
            code.append("    private ")
                    .append(resolveType(attribute.getAttributeType(), "String"))
                    .append(" ")
                    .append(attribute.getAttributeName());
            if (org.springframework.util.StringUtils.hasText(attribute.getDefaultValue())) {
                code.append(" = ").append(attribute.getDefaultValue());
            }
            code.append(";\n\n");
        }
    }
}
