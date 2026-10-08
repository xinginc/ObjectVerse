package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeNote extends BaseEntity {

    private Long userId;

    private String title;

    private String category;

    private String description;

    private String content;

    private String sourceType;

    private String imageUrl;

    private String tags;

    private String visibility;

    private Integer likeCount;

    private Integer favoriteCount;
}
