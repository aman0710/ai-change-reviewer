package com.aichange.reviewer.controller;

import com.aichange.reviewer.model.ChangelogRequest;
import com.aichange.reviewer.service.ChangeReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for changelog generation endpoints.
 * Handles incoming requests for generating AI-powered changelogs from GitHub URLs.
 */
@RestController
@RequestMapping("/api/changelog")
public class ChangeReportController {

    private final ChangeReportService changeReportService;

    public ChangeReportController(ChangeReportService changeReportService) {
        this.changeReportService = changeReportService;
    }

    /**
     * Serve the frontend index page.
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Generate a changelog from a GitHub PR or branch compare URL.
     * @param request the changelog request
     * @return the generated markdown file as an attachment
     */
    @PostMapping(value = "/generate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> generateChangelog(@RequestBody ChangelogRequest request) {
        // Generate the changelog
        String markdownContent = changeReportService.generateChangelog(request);

        // Extract repo name from URL for filename
        String filename = generateFilename(request.getUrl());

        // Set response headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/markdown"));
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(markdownContent, headers, HttpStatus.OK);
    }

    private String generateFilename(String url) {
        // Extract repo name from URL
        String[] parts = url.split("/");
        String repo = parts[parts.length - 3]; // repo name is 3 positions from the end

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return String.format("changelog-%s-%s.md", repo, timestamp);
    }
}

