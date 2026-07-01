package com.aichange.reviewer.service;

import com.aichange.reviewer.builder.ChangeReportBuilder;
import com.aichange.reviewer.model.AiAnalysisResult;
import com.aichange.reviewer.model.ChangelogRequest;
import com.aichange.reviewer.model.CommitInfo;
import com.aichange.reviewer.model.FileChange;
import com.aichange.reviewer.model.GitHubContext;
import com.aichange.reviewer.parser.GitHubUrlParser;
import com.aichange.reviewer.service.github.GitHubFetchStrategy;
import com.aichange.reviewer.service.github.GitHubFetchStrategyFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Main service that orchestrates the changelog generation flow.
 * Coordinates URL parsing, GitHub data fetching, AI analysis, and report building.
 */
@Service
public class ChangeReportService {

    private final GitHubUrlParser urlParser;
    private final GitHubFetchStrategyFactory strategyFactory;
    private final AiAnalysisService aiAnalysisService;
    private final ChangeReportBuilder reportBuilder;

    public ChangeReportService(
        GitHubUrlParser urlParser,
        GitHubFetchStrategyFactory strategyFactory,
        AiAnalysisService aiAnalysisService,
        ChangeReportBuilder reportBuilder
    ) {
        this.urlParser = urlParser;
        this.strategyFactory = strategyFactory;
        this.aiAnalysisService = aiAnalysisService;
        this.reportBuilder = reportBuilder;
    }

    /**
     * Generate a changelog report from a GitHub URL.
     * @param request the changelog request containing URL and optional token
     * @return markdown changelog content
     */
    public String generateChangelog(ChangelogRequest request) {
        // Parse the URL
        GitHubContext context = urlParser.parse(request.getUrl());

        // Resolve the appropriate fetch strategy
        GitHubFetchStrategy strategy = strategyFactory.resolve(context.getType());

        // Fetch commits and file changes from GitHub
        List<CommitInfo> commits = strategy.fetchCommits(context, request.getGithubToken());
        List<FileChange> fileChanges = strategy.fetchFileChanges(context, request.getGithubToken());

        if (commits.isEmpty() && fileChanges.isEmpty()) {
            throw new RuntimeException("No commits or file changes found");
        }

        // Get AI analysis
        AiAnalysisResult analysis = aiAnalysisService.analyze(commits, fileChanges);

        // Build the markdown report
        String report = reportBuilder
            .addHeader(context.getOwner(), context.getRepo(), request.getUrl(), commits.size())
            .addFeatures(analysis.features())
            .addBugFixes(analysis.bugFixes())
            .addImprovements(analysis.improvements())
            .addBusinessImpact(analysis.businessImpact())
            .addRiskConsiderations(analysis.riskConsiderations())
            .addTestScenarios(analysis.testScenarios())
            .build();

        return report;
    }
}
