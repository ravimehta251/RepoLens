package com.reposage.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Slf4j
@Component
public class GitHubClientImpl implements GitHubClient {
  @Value("${github.api-base}")
  private String apiBase;

  @Value("${github.token}")
  private String token;

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
      ".md", ".java", ".py", ".js", ".ts", ".jsx", ".tsx", ".css", ".html"
  );
  private static final Set<String> SKIP_DIRS = Set.of(
      "test", "tests", "target", "node_modules", ".git", "build", "dist", "out"
  );

  public GitHubClientImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
    this.restTemplate = restTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public List<FetchedFile> fetchRepoFiles(String owner, String repo) throws Exception {
    List<FetchedFile> files = new ArrayList<>();
    fetchFilesRecursive(owner, repo, "", files);
    return files;
  }

  private void fetchFilesRecursive(String owner, String repo, String path, List<FetchedFile> files) throws Exception {
    String url = apiBase + "/repos/" + owner + "/" + repo + "/contents" + (path.isEmpty() ? "" : "/" + path);
    
    try {
      ResponseEntity<String> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          new HttpEntity<>(buildHeaders()),
          String.class
      );

      JsonNode nodes = objectMapper.readTree(response.getBody());
      if (!nodes.isArray()) return;

      for (JsonNode node : nodes) {
        String name = node.get("name").asText();
        String type = node.get("type").asText();
        String nodePath = path.isEmpty() ? name : path + "/" + name;

        if ("dir".equals(type)) {
          if (!shouldSkipDir(nodePath)) {
            fetchFilesRecursive(owner, repo, nodePath, files);
          }
        } else if ("file".equals(type)) {
          if (shouldIncludeFile(name)) {
            String downloadUrl = node.get("download_url").asText();
            String content = fetchFileContent(downloadUrl);
            files.add(new FetchedFile(nodePath, content));
          }
        }
      }
    } catch (Exception e) {
      log.warn("Failed to fetch from {}: {}", url, e.getMessage());
    }
  }

  private String fetchFileContent(String downloadUrl) throws Exception {
    ResponseEntity<String> response = restTemplate.exchange(
        downloadUrl,
        HttpMethod.GET,
        new HttpEntity<>(buildHeaders()),
        String.class
    );
    return response.getBody();
  }

  private HttpHeaders buildHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/vnd.github.v3.raw");
    if (token != null && !token.isEmpty()) {
      headers.set("Authorization", "token " + token);
    }
    return headers;
  }

  private boolean shouldIncludeFile(String fileName) {
    for (String ext : ALLOWED_EXTENSIONS) {
      if (fileName.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  private boolean shouldSkipDir(String dirPath) {
    for (String skip : SKIP_DIRS) {
      if (dirPath.contains("/" + skip + "/") || dirPath.endsWith("/" + skip) || dirPath.equals(skip)) {
        return true;
      }
    }
    return false;
  }
}
