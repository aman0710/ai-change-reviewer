package com.aichange.reviewer.model;

/**
 * Request DTO for the changelog generation endpoint.
 */
public class ChangelogRequest {
    private String url;
    private String githubToken;

    public ChangelogRequest() {
    }

    public ChangelogRequest(String url, String githubToken) {
        this.url = url;
        this.githubToken = githubToken;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
