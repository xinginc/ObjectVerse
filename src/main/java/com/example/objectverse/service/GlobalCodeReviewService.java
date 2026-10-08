package com.example.objectverse.service;

import com.example.objectverse.dto.ai.CodeReviewResultDTO;

public interface GlobalCodeReviewService {

    CodeReviewResultDTO analyze(String codeText);
}
