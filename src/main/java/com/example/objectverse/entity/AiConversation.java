package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiConversation extends BaseEntity {

    private Long projectId;

    private Long taskId;

    private String agentName;

    private String promptContent;

    private String responseContent;

    private String modelName;
}
