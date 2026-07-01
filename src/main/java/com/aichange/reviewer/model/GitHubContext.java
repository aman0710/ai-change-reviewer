package com.aichange.reviewer.model;

/**
 * Context object containing parsed GitHub URL information.
 * Used to pass state through the GitHub fetch strategies.
 */
public class GitHubContext {
    private final String owner;
    private final String repo;
    private final String prNumber;
    private final String baseBranch;
    private final String headBranch;
    private final GitHubUrlType type;

    private GitHubContext(String owner, String repo, String prNumber, String baseBranch, String headBranch, GitHubUrlType type) {
        this.owner = owner;
        this.repo = repo;
        this.prNumber = prNumber;
        this.baseBranch = baseBranch;
        this.headBranch = headBranch;
        this.type = type;
    }

    public static GitHubContext forPullRequest(String owner, String repo, String prNumber) {
        return new GitHubContext(owner, repo, prNumber, null, null, GitHubUrlType.PULL_REQUEST);
    }

    public static GitHubContext forBranchCompare(String owner, String repo, String baseBranch, String headBranch) {
        return new GitHubContext(owner, repo, null, baseBranch, headBranch, GitHubUrlType.BRANCH_COMPARE);
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public String getPrNumber() {
        return prNumber;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public GitHubUrlType getType() {
        return type;
    }
}
