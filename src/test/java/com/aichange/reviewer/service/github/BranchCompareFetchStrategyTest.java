package com.aichange.reviewer.service.github;

import com.aichange.reviewer.exception.GitHubApiException;
import com.aichange.reviewer.model.CommitInfo;
import com.aichange.reviewer.model.FileChange;
import com.aichange.reviewer.model.GitHubContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BranchCompareFetchStrategy.
 */
@ExtendWith(MockitoExtension.class)
class BranchCompareFetchStrategyTest {

    @Mock
    private RestTemplate restTemplate;

    private BranchCompareFetchStrategy strategy;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        strategy = new BranchCompareFetchStrategy(restTemplate, objectMapper);
        ReflectionTestUtils.setField(strategy, "githubBaseUrl", "https://api.github.com");
    }

    @Test
    void testFetchCommitsSuccess() {
        GitHubContext context = GitHubContext.forBranchCompare("user", "repo", "main", "feature");

        String mockResponse = """
            {
                "commits": [
                    {
                        "sha": "abc123",
                        "commit": {
                            "message": "Add feature",
                            "author": {"name": "Alice"}
                        }
                    }
                ],
                "files": []
            }
            """;

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<CommitInfo> commits = strategy.fetchCommits(context, null);

        assertEquals(1, commits.size());
        assertEquals("abc123", commits.get(0).sha());
        assertEquals("Add feature", commits.get(0).message());
    }

    @Test
    void testFetchFileChangesSuccess() {
        GitHubContext context = GitHubContext.forBranchCompare("user", "repo", "main", "feature");

        String mockResponse = """
            {
                "commits": [],
                "files": [
                    {
                        "filename": "README.md",
                        "status": "modified",
                        "additions": 10,
                        "deletions": 5,
                        "patch": "@@ -1,5 +1,10 @@"
                    }
                ]
            }
            """;

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<FileChange> changes = strategy.fetchFileChanges(context, null);

        assertEquals(1, changes.size());
        assertEquals("README.md", changes.get(0).filename());
        assertEquals("modified", changes.get(0).status());
    }

    @Test
    void testFetchCommitsUnauthorized() {
        GitHubContext context = GitHubContext.forBranchCompare("private", "repo", "main", "feature");

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(GitHubApiException.class, () -> strategy.fetchCommits(context, null));
    }
}
