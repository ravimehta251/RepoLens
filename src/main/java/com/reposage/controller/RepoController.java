package com.reposage.controller;

import com.reposage.dto.*;
import com.reposage.entity.Repo;
import com.reposage.repository.CodeChunkRepository;
import com.reposage.repository.RepoRepository;
import com.reposage.service.IngestService;
import com.reposage.util.GitHubUrlParser;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/repos")
public class RepoController {
  private final RepoRepository repoRepository;
  private final CodeChunkRepository codeChunkRepository;
  private final IngestService ingestService;

  public RepoController(
      RepoRepository repoRepository,
      CodeChunkRepository codeChunkRepository,
      IngestService ingestService
  ) {
    this.repoRepository = repoRepository;
    this.codeChunkRepository = codeChunkRepository;
    this.ingestService = ingestService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<IndexRepoResponse>> indexRepo(@Valid @RequestBody IndexRepoRequest request) {
    try {
      String url = request.getGithubUrl().trim();
      
      // Check if already indexed
      var existing = repoRepository.findByUrl(url);
      if (existing.isPresent()) {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(ApiResponse.ok(IndexRepoResponse.builder()
                .repoId(existing.get().getId())
                .status(existing.get().getStatus())
                .build()));
      }

      // Parse GitHub URL
      GitHubUrlParser.GitHubRepo ghRepo = GitHubUrlParser.parseUrl(url);

      // Create repo entity
      Repo repo = Repo.builder()
          .owner(ghRepo.owner())
          .name(ghRepo.name())
          .url(url)
          .status("PENDING")
          .build();
      Repo saved = repoRepository.save(repo);

      // Trigger async ingestion
      ingestService.ingestRepo(saved.getId());

      return ResponseEntity
          .status(HttpStatus.ACCEPTED)
          .body(ApiResponse.ok(IndexRepoResponse.builder()
              .repoId(saved.getId())
              .status(saved.getStatus())
              .build()));

    } catch (IllegalArgumentException e) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      log.error("Error indexing repo", e);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Failed to index repository: " + e.getMessage()));
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<RepoStatusResponse>> getRepoStatus(@PathVariable Long id) {
    try {
      Repo repo = repoRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Repository not found"));

      int chunkCount = (int) codeChunkRepository.countByRepoId(id);

      RepoStatusResponse response = RepoStatusResponse.builder()
          .repoId(repo.getId())
          .owner(repo.getOwner())
          .name(repo.getName())
          .status(repo.getStatus())
          .chunkCount(chunkCount)
          .build();

      return ResponseEntity.ok(ApiResponse.ok(response));

    } catch (Exception e) {
      log.error("Error fetching repo status", e);
      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.fail("Repository not found"));
    }
  }
}
