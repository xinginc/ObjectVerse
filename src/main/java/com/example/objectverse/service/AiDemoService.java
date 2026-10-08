package com.example.objectverse.service;

public interface AiDemoService {

    String runRequirementAnalysis(Long projectId, String userInput);

    String runCodeReview(Long projectId, String userInput);
}
