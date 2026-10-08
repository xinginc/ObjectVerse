package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeNoteReaction extends BaseEntity {

    private Long noteId;

    private Long userId;

    private String reactionType;
}
