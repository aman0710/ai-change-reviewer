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
 * Unit tests for PullRequestFetchStrategy.
 */
@ExtendWith(MockitoExtension.class)
class PullRequestFetchStrategyTest {

    @Mock
    private RestTemplate restTemplate;

    private PullRequestFetchStrategy strategy;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        strategy = new PullRequestFetchStrategy(restTemplate, objectMapper);
        ReflectionTestUtils.setField(strategy, "githubBaseUrl", "https://api.github.com");
    }

    @Test
    void testFetchCommitsSuccess() {
        GitHubContext context = GitHubContext.forPullRequest("user", "repo", "123");

        String mockResponse = """
            [
                {
                    "sha": "abc123",
                    "commit": {
                        "message": "Add new feature",
                        "author": {"name": "John Doe"}
                    }
                },
                {
                    "sha": "def456",
                    "commit": {
                        "message": "Fix bug",
                        "author": {"name": "Jane Smith"}
                    }
                }
            ]
            """;

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<CommitInfo> commits = strategy.fetchCommits(context, null);

        assertEquals(2, commits.size());
        assertEquals("abc123", commits.get(0).sha());
        assertEquals("Add new feature", commits.get(0).message());
        assertEquals("John Doe", commits.get(0).author());
    }

    @Test
    void testFetchCommitsNotFound() {
        GitHubContext context = GitHubContext.forPullRequest("user", "repo", "999");

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(GitHubApiException.class, () -> strategy.fetchCommits(context, null));
    }

    @Test
    void testFetchFileChangesSuccess() {
        GitHubContext context = GitHubContext.forPullRequest("user", "repo", "123");

        String mockResponse = """
            [
                {
                    "filename": "src/Main.java",
                    "status": "added",
                    "additions": 50,
                    "deletions": 0,
                    "patch": "@@ -0,0 +1,50 @@\\n+public class Main {\\n+}"
                }
            ]
            """;

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        List<FileChange> changes = strategy.fetchFileChanges(context, null);

        assertEquals(1, changes.size());
        assertEquals("src/Main.java", changes.get(0).filename());
        assertEquals("added", changes.get(0).status());
        assertEquals(50, changes.get(0).additions());
        assertEquals(0, changes.get(0).deletions());
    }
}
