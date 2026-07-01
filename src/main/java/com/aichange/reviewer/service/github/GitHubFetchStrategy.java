package com.aichange.reviewer.service.github;

import com.aichange.reviewer.model.CommitInfo;
import com.aichange.reviewer.model.FileChange;
import com.aichange.reviewer.model.GitHubContext;

import java.util.List;

/**
 * Strategy interface for fetching GitHub commit and file change information.
 * Different URL types (PR vs branch compare) require different GitHub API calls.
 */
public interface GitHubFetchStrategy {

    /**
     * Fetch commits for the given GitHub context.
     * @param context the parsed GitHub URL context
     * @param token optional GitHub token for private repos
     * @return list of commits
     */
    List<CommitInfo> fetchCommits(GitHubContext context, String token);

    /**
     * Fetch file changes for the given GitHub context.
     * @param context the parsed GitHub URL context
     * @param token optional GitHub token for private repos
     * @return list of file changes
     */
    List<FileChange> fetchFileChanges(GitHubContext context, String token);
}
