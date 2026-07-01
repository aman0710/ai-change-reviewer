package com.aichange.reviewer.service;

import com.aichange.reviewer.exception.AiServiceException;
import com.aichange.reviewer.model.AiAnalysisResult;
import com.aichange.reviewer.model.CommitInfo;
import com.aichange.reviewer.model.FileChange;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Service for calling the Google Gemini API to analyze code changes.
 * Constructs a detailed prompt with commit and file change context,
 * sends it to Gemini, and parses the response into structured sections.
 */
@Service
public class AiAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public AiAnalysisService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Analyze commits and file changes using the Gemini API.
     * @param commits list of commits to analyze
     * @param fileChanges list of file changes to analyze
     * @return AI analysis result with sections
     * @throws AiServiceException if the API call fails
     */
    public AiAnalysisResult analyze(List<CommitInfo> commits, List<FileChange> fileChanges) throws AiServiceException {
        if (commits.isEmpty() && fileChanges.isEmpty()) {
            throw new AiServiceException("No commits or file changes provided for analysis");
        }

        String prompt = constructPrompt(commits, fileChanges);
        String response = callGeminiApi(prompt);
        return parseResponse(response);
    }

    private String constructPrompt(List<CommitInfo> commits, List<FileChange> fileChanges) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a code review expert. Analyze the following code changes and provide a structured report.\n\n");

        // Add commit information
        prompt.append("## Commits:\n");
        for (CommitInfo commit : commits) {
            prompt.append(String.format("- SHA: %s\n  Message: %s\n  Author: %s\n", 
                commit.sha(), commit.message(), commit.author()));
        }

        // Add file changes
        prompt.append("\n## File Changes:\n");
        for (FileChange change : fileChanges) {
            String diffPreview = change.patch() != null && change.patch().length() > 500 
                ? change.patch().substring(0, 500) + "..." 
                : (change.patch() != null ? change.patch() : "");
            prompt.append(String.format("- %s (%s): +%d -%d\n  Diff: %s\n",
                change.filename(), change.status(), change.additions(), change.deletions(), diffPreview));
        }

        // Add analysis instructions
        prompt.append("\n## Analysis Instructions:\n");
        prompt.append("Provide analysis in the following format (use these exact markers):\n");
        prompt.append("### FEATURES\n[List of new features added]\n\n");
        prompt.append("### BUG_FIXES\n[List of bug fixes]\n\n");
        prompt.append("### IMPROVEMENTS\n[Improvements and refactors]\n\n");
        prompt.append("### BUSINESS_IMPACT\n[Business impact analysis]\n\n");
        prompt.append("### RISK_CONSIDERATIONS\n[Risks and concerns]\n\n");
        prompt.append("### TEST_SCENARIOS\n[Recommended test scenarios]\n\n");
        prompt.append("If a section has nothing significant, write 'Nothing significant'.\n");
        prompt.append("Ignore noise commits (like 'wip', 'fix', 'minor', 'temp').\n");
        prompt.append("Write in plain, human-readable language.\n");

        return prompt.toString();
    }

    private String callGeminiApi(String prompt) throws AiServiceException {
        try {
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            // Build the request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.putArray("contents")
                .addObject()
                .putArray("parts")
                .addObject()
                .put("text", prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getBody() == null || response.getBody().isEmpty()) {
                throw new AiServiceException("Empty response from Gemini API");
            }

            // Extract text from response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            JsonNode candidates = jsonResponse.path("candidates");
            
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content").path("parts").get(0).path("text");
                return content.asText();
            }

            throw new AiServiceException("Unexpected response format from Gemini API");
        } catch (RestClientException e) {
            throw new AiServiceException("Failed to call Gemini API: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AiServiceException("Error processing Gemini API response: " + e.getMessage(), e);
        }
    }

    private AiAnalysisResult parseResponse(String response) throws AiServiceException {
        try {
            String features = extractSection(response, "### FEATURES");
            String bugFixes = extractSection(response, "### BUG_FIXES");
            String improvements = extractSection(response, "### IMPROVEMENTS");
            String businessImpact = extractSection(response, "### BUSINESS_IMPACT");
            String riskConsiderations = extractSection(response, "### RISK_CONSIDERATIONS");
            String testScenarios = extractSection(response, "### TEST_SCENARIOS");

            return new AiAnalysisResult(features, bugFixes, improvements, businessImpact, riskConsiderations, testScenarios);
        } catch (Exception e) {
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

    private String extractSection(String response, String marker) {
        int startIdx = response.indexOf(marker);
        if (startIdx == -1) {
            return "Nothing significant";
        }

        startIdx += marker.length();
        int endIdx = response.indexOf("###", startIdx);
        if (endIdx == -1) {
            endIdx = response.length();
        }

        String section = response.substring(startIdx, endIdx).trim();
        return section.isEmpty() ? "Nothing significant" : section;
    }
}
