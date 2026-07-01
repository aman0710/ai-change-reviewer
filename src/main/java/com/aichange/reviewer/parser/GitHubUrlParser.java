package com.aichange.reviewer.parser;

import com.aichange.reviewer.exception.InvalidUrlException;
import com.aichange.reviewer.model.GitHubContext;
import com.aichange.reviewer.model.GitHubUrlType;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for GitHub URLs.
 * Supports both PR URLs (https://github.com/{owner}/{repo}/pull/{number})
 * and branch compare URLs (https://github.com/{owner}/{repo}/compare/{base}...{head})
 */
@Component
public class GitHubUrlParser {

    private static final Pattern PR_PATTERN = Pattern.compile(
        "https://github\\.com/([^/]+)/([^/]+)/pull/(\\d+)"
    );

    private static final Pattern BRANCH_COMPARE_PATTERN = Pattern.compile(
        "https://github\\.com/([^/]+)/([^/]+)/compare/([^.]+)\\.\\.\\.(.+)"
    );

    /**
     * Parse a GitHub URL and extract context information.
     * @param url the GitHub URL to parse
     * @return GitHubContext containing parsed information
     * @throws InvalidUrlException if the URL format is invalid
     */
    public GitHubContext parse(String url) throws InvalidUrlException {
        if (url == null || url.trim().isEmpty()) {
            throw new InvalidUrlException("URL cannot be null or empty");
        }

        url = url.trim();

        // Try PR pattern first
        Matcher prMatcher = PR_PATTERN.matcher(url);
        if (prMatcher.matches()) {
            String owner = prMatcher.group(1);
            String repo = prMatcher.group(2);
            String prNumber = prMatcher.group(3);
            return GitHubContext.forPullRequest(owner, repo, prNumber);
        }

        // Try branch compare pattern
        Matcher compareMatcher = BRANCH_COMPARE_PATTERN.matcher(url);
        if (compareMatcher.matches()) {
            String owner = compareMatcher.group(1);
            String repo = compareMatcher.group(2);
            String baseBranch = compareMatcher.group(3);
            String headBranch = compareMatcher.group(4);
            return GitHubContext.forBranchCompare(owner, repo, baseBranch, headBranch);
        }

        throw new InvalidUrlException(
            "Invalid GitHub URL format. Supported formats: " +
            "https://github.com/{owner}/{repo}/pull/{number} or " +
            "https://github.com/{owner}/{repo}/compare/{base}...{head}"
        );
    }
}
