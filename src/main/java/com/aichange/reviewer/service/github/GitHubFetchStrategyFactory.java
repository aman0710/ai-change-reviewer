package com.aichange.reviewer.service.github;

import com.aichange.reviewer.model.GitHubUrlType;
import org.springframework.stereotype.Component;

/**
 * Factory for resolving the appropriate GitHub fetch strategy based on URL type.
 * This ensures the service layer doesn't need to perform instanceof checks.
 */
@Component
public class GitHubFetchStrategyFactory {

    private final PullRequestFetchStrategy pullRequestFetchStrategy;
    private final BranchCompareFetchStrategy branchCompareFetchStrategy;

    public GitHubFetchStrategyFactory(
        PullRequestFetchStrategy pullRequestFetchStrategy,
        BranchCompareFetchStrategy branchCompareFetchStrategy
    ) {
        this.pullRequestFetchStrategy = pullRequestFetchStrategy;
        this.branchCompareFetchStrategy = branchCompareFetchStrategy;
    }

    /**
     * Resolve the appropriate strategy for the given URL type.
     * @param type the type of GitHub URL
     * @return the corresponding fetch strategy
     */
    public GitHubFetchStrategy resolve(GitHubUrlType type) {
        return switch (type) {
            case PULL_REQUEST -> pullRequestFetchStrategy;
            case BRANCH_COMPARE -> branchCompareFetchStrategy;
        };
    }
}
