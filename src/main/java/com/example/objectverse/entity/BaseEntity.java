package com.example.objectverse.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class BaseEntity {

    private Long id;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer deleted;
}
