package com.aichange.reviewer.config;

/**
 * Configuration properties for the application.
 * Read from application.properties and environment variables.
 */
public class AppProperties {

    private String githubBaseUrl = "https://api.github.com";
    private String geminiKey;
    private String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    public String getGithubBaseUrl() {
        return githubBaseUrl;
    }

    public void setGithubBaseUrl(String githubBaseUrl) {
        this.githubBaseUrl = githubBaseUrl;
    }

    public String getGeminiKey() {
        return geminiKey;
    }

    public void setGeminiKey(String geminiKey) {
        this.geminiKey = geminiKey;
    }

    public String getGeminiUrl() {
        return geminiUrl;
    }

    public void setGeminiUrl(String geminiUrl) {
        this.geminiUrl = geminiUrl;
    }
}
