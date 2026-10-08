package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CodeFile extends BaseEntity {

    private Long projectId;

    private String fileName;

    private String packageName;

    private String fileType;

    private String codeContent;
}
