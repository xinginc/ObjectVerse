package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PracticeQuestion extends BaseEntity {

    private Long userId;

    private Long knowledgeNoteId;

    private String questionType;

    private String title;

    private String body;

    private String answer;
}
