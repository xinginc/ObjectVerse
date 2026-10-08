package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopInterface extends BaseEntity {

    private Long projectId;

    private String interfaceName;

    private String packageName;

    private String description;

    private String visibility;
}
