package com.reposage.controller;

import com.reposage.dto.*;
import com.reposage.entity.CodeChunk;
import com.reposage.repository.CodeChunkRepository;
import com.reposage.repository.RepoRepository;
import com.reposage.service.RagQueryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/repos/{id}")
public class QueryController {
  private final RagQueryService ragQueryService;
  private final CodeChunkRepository codeChunkRepository;
  private final RepoRepository repoRepository;

  public QueryController(
      RagQueryService ragQueryService,
      CodeChunkRepository codeChunkRepository,
      RepoRepository repoRepository
  ) {
    this.ragQueryService = ragQueryService;
    this.codeChunkRepository = codeChunkRepository;
    this.repoRepository = repoRepository;
  }

  @PostMapping("/query")
  public ResponseEntity<ApiResponse<QueryResponse>> query(
      @PathVariable Long id,
      @Valid @RequestBody QueryRequest request
  ) {
    try {
      repoRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Repository not found"));

      QueryResponse response = ragQueryService.query(id, request.getQuestion());
      return ResponseEntity.ok(ApiResponse.ok(response));

    } catch (IllegalArgumentException e) {
      return ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not ready")) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.fail("Repository is not ready for querying"));
      }
      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.fail(e.getMessage()));
    } catch (Exception e) {
      log.error("Error processing query", e);
      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.fail("Failed to process query: " + e.getMessage()));
    }
  }

  @GetMapping("/chunks")
  public ResponseEntity<ApiResponse<List<CodeChunk>>> getChunks(@PathVariable Long id) {
    try {
      repoRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Repository not found"));

      List<CodeChunk> chunks = codeChunkRepository.findByRepoId(id);
      return ResponseEntity.ok(ApiResponse.ok(chunks));

    } catch (Exception e) {
      log.error("Error fetching chunks", e);
      return ResponseEntity
          .status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.fail("Repository not found"));
    }
  }
}
