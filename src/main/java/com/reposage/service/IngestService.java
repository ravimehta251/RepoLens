package com.reposage.service;

import com.reposage.client.EmbeddingClient;
import com.reposage.client.GitHubClient;
import com.reposage.entity.CodeChunk;
import com.reposage.entity.Repo;
import com.reposage.repository.CodeChunkRepository;
import com.reposage.repository.RepoRepository;
import com.reposage.util.GitHubUrlParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class IngestService {
  private final RepoRepository repoRepository;
  private final CodeChunkRepository codeChunkRepository;
  private final GitHubClient gitHubClient;
  private final EmbeddingClient embeddingClient;
  private final ChunkService chunkService;

  public IngestService(
      RepoRepository repoRepository,
      CodeChunkRepository codeChunkRepository,
      GitHubClient gitHubClient,
      EmbeddingClient embeddingClient,
      ChunkService chunkService
  ) {
    this.repoRepository = repoRepository;
    this.codeChunkRepository = codeChunkRepository;
    this.gitHubClient = gitHubClient;
    this.embeddingClient = embeddingClient;
    this.chunkService = chunkService;
  }

  @Async
  @Transactional
  public void ingestRepo(Long repoId) {
    Repo repo = repoRepository.findById(repoId)
        .orElseThrow(() -> new RuntimeException("Repo not found: " + repoId));

    try {
      repo.setStatus("INDEXING");
      repoRepository.save(repo);
      log.info("Starting ingestion for repo {}", repo.getId());

      // Parse URL to get owner/name
      GitHubUrlParser.GitHubRepo ghRepo = GitHubUrlParser.parseUrl(repo.getUrl());

      // Step 1: Fetch files from GitHub
      List<GitHubClient.FetchedFile> files = gitHubClient.fetchRepoFiles(ghRepo.owner(), ghRepo.name());
      log.info("Fetched {} files from {}/{}", files.size(), ghRepo.owner(), ghRepo.name());

      // Step 2: Chunk files
      List<CodeChunk> allChunks = new java.util.ArrayList<>();
      for (GitHubClient.FetchedFile file : files) {
        List<CodeChunk> chunks = chunkService.chunkFile(file.path(), file.content(), repoId);
        allChunks.addAll(chunks);
      }
      log.info("Created {} chunks", allChunks.size());

      // Step 3: Embed chunks
      int count = 0;
      for (CodeChunk chunk : allChunks) {
        try {
          var embedding = embeddingClient.embed(chunk.getChunkText());
          chunk.setEmbedding(embedding);
          count++;
          
          if (count % 10 == 0) {
            log.info("Embedded {} chunks", count);
          }
        } catch (Exception e) {
          log.warn("Failed to embed chunk {}: {}", chunk.getId(), e.getMessage());
        }
      }

      // Step 4: Save chunks
      codeChunkRepository.saveAll(allChunks);
      log.info("Saved all {} chunks to DB", allChunks.size());

      // Step 5: Mark repo as ready
      repo.setStatus("READY");
      repo.setIndexedAt(LocalDateTime.now());
      repoRepository.save(repo);
      log.info("Repo {} ingestion complete", repo.getId());

    } catch (Exception e) {
      log.error("Ingestion failed for repo {}: {}", repo.getId(), e.getMessage(), e);
      repo.setStatus("FAILED");
      repoRepository.save(repo);
    }
  }
}
