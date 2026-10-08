package com.example.objectverse.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReviewResult extends BaseEntity {

    private Long projectId;

    private String reviewType;

    private BigDecimal score;

    private String problemSummary;

    private String suggestion;
}
