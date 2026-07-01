package com.aichange.reviewer.service.github;

import com.aichange.reviewer.exception.GitHubApiException;
import com.aichange.reviewer.model.CommitInfo;
import com.aichange.reviewer.model.FileChange;
import com.aichange.reviewer.model.GitHubContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for fetching commits and files from a branch compare.
 * Uses the /repos/{owner}/{repo}/compare/{base}...{head} endpoint.
 */
@Component
public class BranchCompareFetchStrategy implements GitHubFetchStrategy {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubBaseUrl;

    public BranchCompareFetchStrategy(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CommitInfo> fetchCommits(GitHubContext context, String token) {
        String url = String.format(
            "%s/repos/%s/%s/compare/%s...%s",
            githubBaseUrl, context.getOwner(), context.getRepo(),
            context.getBaseBranch(), context.getHeadBranch()
        );

        return fetchAndParseCommits(url, token);
    }

    @Override
    public List<FileChange> fetchFileChanges(GitHubContext context, String token) {
        String url = String.format(
            "%s/repos/%s/%s/compare/%s...%s",
            githubBaseUrl, context.getOwner(), context.getRepo(),
            context.getBaseBranch(), context.getHeadBranch()
        );

        return fetchAndParseFileChanges(url, token);
    }

    private List<CommitInfo> fetchAndParseCommits(String url, String token) {
        try {
            HttpHeaders headers = buildHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            List<CommitInfo> commits = new ArrayList<>();
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode commitsArray = root.path("commits");
            if (commitsArray.isArray()) {
                for (JsonNode node : commitsArray) {
                    String sha = node.path("sha").asText();
                    String message = node.path("commit").path("message").asText();
                    String author = node.path("commit").path("author").path("name").asText();
                    commits.add(new CommitInfo(sha, message, author));
                }
            }

            return commits;
        } catch (HttpClientErrorException e) {
            throw new GitHubApiException(
                "Failed to fetch commits from GitHub: " + e.getMessage(),
                e.getStatusCode().value(),
                e
            );
        } catch (Exception e) {
            throw new GitHubApiException("Failed to parse GitHub commits response: " + e.getMessage(), 0, e);
        }
    }

    private List<FileChange> fetchAndParseFileChanges(String url, String token) {
        try {
            HttpHeaders headers = buildHeaders(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            List<FileChange> fileChanges = new ArrayList<>();
            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode filesArray = root.path("files");
            if (filesArray.isArray()) {
                for (JsonNode node : filesArray) {
                    String filename = node.path("filename").asText();
                    String status = node.path("status").asText();
                    int additions = node.path("additions").asInt();
                    int deletions = node.path("deletions").asInt();
                    String patch = node.path("patch").asText("");

                    fileChanges.add(new FileChange(filename, status, additions, deletions, patch));
                }
            }

            return fileChanges;
        } catch (HttpClientErrorException e) {
            throw new GitHubApiException(
                "Failed to fetch file changes from GitHub: " + e.getMessage(),
                e.getStatusCode().value(),
                e
            );
        } catch (Exception e) {
            throw new GitHubApiException("Failed to parse GitHub file changes response: " + e.getMessage(), 0, e);
        }
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3+json");
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }
}
