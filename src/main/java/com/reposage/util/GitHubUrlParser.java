package com.reposage.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GitHubUrlParser {

  private static final String GITHUB_URL_PATTERN = "^https://github\\.com/([a-zA-Z0-9_-]+)/([a-zA-Z0-9_-]+)(\\.git)?/?$";

  public record GitHubRepo(String owner, String name) {}

  public static GitHubRepo parseUrl(String url) throws IllegalArgumentException {
    if (url == null || url.isEmpty()) {
      throw new IllegalArgumentException("GitHub URL cannot be empty");
    }

    // Normalize URL
    String normalized = url.trim();
    if (!normalized.startsWith("https://")) {
      normalized = "https://" + normalized;
    }

    // Remove trailing .git and slashes
    normalized = normalized.replaceAll("\\.git/?$", "").replaceAll("/$", "");

    String[] parts = normalized.split("/");
    
    if (parts.length < 5 || !parts[2].equals("github.com")) {
      throw new IllegalArgumentException("Invalid GitHub URL format. Expected: https://github.com/owner/repo");
    }

    String owner = parts[3];
    String repo = parts[4];

    if (owner.isEmpty() || repo.isEmpty()) {
      throw new IllegalArgumentException("Owner and repo name cannot be empty");
    }

    log.info("Parsed GitHub URL: owner={}, repo={}", owner, repo);
    return new GitHubRepo(owner, repo);
  }
}
