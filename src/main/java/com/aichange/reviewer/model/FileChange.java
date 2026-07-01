package com.aichange.reviewer.model;

/**
 * Record representing a file change from the GitHub API.
 */
public record FileChange(
    String filename,
    String status,      // added, modified, deleted, renamed, etc.
    int additions,
    int deletions,
    String patch       // diff content
) {}
