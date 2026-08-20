package com.reposage.client;

import java.util.List;

public interface GitHubClient {
  record FetchedFile(String path, String content) {}

  List<FetchedFile> fetchRepoFiles(String owner, String repo) throws Exception;
}
