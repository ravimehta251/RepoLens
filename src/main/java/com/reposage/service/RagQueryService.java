package com.reposage.service;

import com.reposage.client.EmbeddingClient;
import com.reposage.client.LLMClient;
import com.reposage.dto.QueryResponse;
import com.reposage.dto.SourceRef;
import com.reposage.entity.CodeChunk;
import com.reposage.entity.QueryLog;
import com.reposage.entity.Repo;
import com.reposage.repository.CodeChunkRepository;
import com.reposage.repository.QueryLogRepository;
import com.reposage.repository.RepoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class RagQueryService {
  @Value("${rag.top-k:5}")
  private int topK;

  private final CodeChunkRepository codeChunkRepository;
  private final QueryLogRepository queryLogRepository;
  private final RepoRepository repoRepository;
  private final EmbeddingClient embeddingClient;
  private final LLMClient llmClient;
  private final ObjectMapper objectMapper;

  public RagQueryService(
      CodeChunkRepository codeChunkRepository,
      QueryLogRepository queryLogRepository,
      RepoRepository repoRepository,
      EmbeddingClient embeddingClient,
      LLMClient llmClient,
      ObjectMapper objectMapper
  ) {
    this.codeChunkRepository = codeChunkRepository;
    this.queryLogRepository = queryLogRepository;
    this.repoRepository = repoRepository;
    this.embeddingClient = embeddingClient;
    this.llmClient = llmClient;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public QueryResponse query(Long repoId, String question) throws Exception {
    Repo repo = repoRepository.findById(repoId)
        .orElseThrow(() -> new RuntimeException("Repo not found"));

    if (!"READY".equals(repo.getStatus())) {
      throw new RuntimeException("Repository is not ready for querying");
    }

    // Step 1: Embed the question
    var questionEmbedding = embeddingClient.embed(question);
    String embeddingString = questionEmbedding.toString();

    // Step 2: Retrieve top-k similar chunks
    List<CodeChunk> chunks = codeChunkRepository.findSimilarChunks(
        repoId,
        embeddingString,
        topK
    );

    // Step 3: Build prompt
    String prompt = buildPrompt(repo, question, chunks);
    log.info("Built prompt for query: {}", question.substring(0, Math.min(50, question.length())));

    // Step 4: Call LLM
    String answer = llmClient.chat(prompt);
    log.info("Got answer from LLM");

    // Step 5: Extract sources
    List<SourceRef> sources = chunks.stream()
        .map(chunk -> SourceRef.builder()
            .filePath(chunk.getFilePath())
            .startLine(chunk.getStartLine())
            .endLine(chunk.getEndLine())
            .build())
        .distinct()
        .toList();

    // Step 6: Log query
    String sourceFilesJson = objectMapper.writeValueAsString(sources);
    QueryLog log = QueryLog.builder()
        .repoId(repoId)
        .question(question)
        .answer(answer)
        .sourceFiles(sourceFilesJson)
        .build();
    queryLogRepository.save(log);

    return QueryResponse.builder()
        .answer(answer)
        .sources(sources)
        .build();
  }

  private String buildPrompt(Repo repo, String question, List<CodeChunk> chunks) {
    StringBuilder contextBuilder = new StringBuilder();
    for (CodeChunk chunk : chunks) {
      contextBuilder.append("--- ")
          .append(chunk.getFilePath());
      
      if (chunk.getStartLine() != null && chunk.getEndLine() != null) {
        contextBuilder.append(" (lines ").append(chunk.getStartLine())
            .append("-").append(chunk.getEndLine()).append(")");
      }
      
      contextBuilder.append(" ---\n")
          .append(chunk.getChunkText())
          .append("\n\n");
    }

    return String.format(
        "You are a code assistant answering questions about the GitHub repository \"%s/%s\".\n" +
        "Use ONLY the following retrieved context to answer. If the context is insufficient, say so.\n\n" +
        "Context:\n%s\n" +
        "Question: %s\n\n" +
        "Answer concisely. Cite file paths inline where relevant.",
        repo.getOwner(),
        repo.getName(),
        contextBuilder.toString(),
        question
    );
  }
}
