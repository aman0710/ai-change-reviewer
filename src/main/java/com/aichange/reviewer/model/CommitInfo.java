package com.aichange.reviewer.model;

/**
 * Record representing a commit from the GitHub API.
 */
public record CommitInfo(
    String sha,
    String message,
    String author
) {}
