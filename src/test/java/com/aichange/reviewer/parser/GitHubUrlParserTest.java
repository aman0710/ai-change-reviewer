package com.aichange.reviewer.parser;

import com.aichange.reviewer.exception.InvalidUrlException;
import com.aichange.reviewer.model.GitHubContext;
import com.aichange.reviewer.model.GitHubUrlType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitHubUrlParser.
 */
class GitHubUrlParserTest {

    private final GitHubUrlParser parser = new GitHubUrlParser();

    @Test
    void testParsePullRequestUrl() {
        String url = "https://github.com/aman0710/ai-change-reviewer/pull/123";
        GitHubContext context = parser.parse(url);

        assertEquals("aman0710", context.getOwner());
        assertEquals("ai-change-reviewer", context.getRepo());
        assertEquals("123", context.getPrNumber());
        assertEquals(GitHubUrlType.PULL_REQUEST, context.getType());
        assertNull(context.getBaseBranch());
        assertNull(context.getHeadBranch());
    }

    @Test
    void testParseBranchCompareUrl() {
        String url = "https://github.com/aman0710/ai-change-reviewer/compare/main...feature-branch";
        GitHubContext context = parser.parse(url);

        assertEquals("aman0710", context.getOwner());
        assertEquals("ai-change-reviewer", context.getRepo());
        assertEquals("main", context.getBaseBranch());
        assertEquals("feature-branch", context.getHeadBranch());
        assertEquals(GitHubUrlType.BRANCH_COMPARE, context.getType());
        assertNull(context.getPrNumber());
    }

    @Test
    void testParseWithTrailingSlash() {
        String url = "https://github.com/user/repo/pull/456/";
        assertThrows(InvalidUrlException.class, () -> parser.parse(url));
    }

    @Test
    void testParseInvalidUrl() {
        String url = "https://github.com/user/repo/invalid";
        assertThrows(InvalidUrlException.class, () -> parser.parse(url));
    }

    @Test
    void testParseNullUrl() {
        assertThrows(InvalidUrlException.class, () -> parser.parse(null));
    }

    @Test
    void testParseEmptyUrl() {
        assertThrows(InvalidUrlException.class, () -> parser.parse(""));
    }

    @Test
    void testParseBranchWithSlash() {
        String url = "https://github.com/user/repo/compare/feature/with-slash...main";
        GitHubContext context = parser.parse(url);

        assertEquals("user", context.getOwner());
        assertEquals("repo", context.getRepo());
        assertEquals("feature/with-slash", context.getBaseBranch());
        assertEquals("main", context.getHeadBranch());
    }
}
