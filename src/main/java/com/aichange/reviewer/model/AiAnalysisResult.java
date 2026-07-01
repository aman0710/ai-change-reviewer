package com.aichange.reviewer.model;

/**
 * Record representing the AI analysis result from Gemini API.
 */
public record AiAnalysisResult(
    String features,
    String bugFixes,
    String improvements,
    String businessImpact,
    String riskConsiderations,
    String testScenarios
) {}
