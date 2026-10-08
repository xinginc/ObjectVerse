package com.example.objectverse.dto.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeReviewResultDTO {

    private String summary;

    private List<String> issues = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    private List<KnowledgePointDTO> knowledgePoints = new ArrayList<>();

    private String improvedCode;

    private String noteContent;

    private String rawResponse;
}
