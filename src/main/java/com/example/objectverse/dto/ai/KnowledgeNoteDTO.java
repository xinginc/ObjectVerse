package com.example.objectverse.dto.ai;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeNoteDTO {

    private String title;

    private String category;

    private String content;

    private LocalDateTime createTime;
}
