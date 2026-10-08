package com.example.objectverse.dto.ai;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequirementChatMessageDTO {

    private String role;

    private String content;

    private LocalDateTime createTime;
}
