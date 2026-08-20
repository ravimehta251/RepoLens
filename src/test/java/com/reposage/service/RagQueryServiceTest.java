package com.reposage.service;

import com.reposage.client.EmbeddingClient;
import com.reposage.client.LLMClient;
import com.reposage.entity.CodeChunk;
import com.reposage.entity.Repo;
import com.reposage.repository.CodeChunkRepository;
import com.reposage.repository.QueryLogRepository;
import com.reposage.repository.RepoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.pgvector.hibernate.Vector;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagQueryServiceTest {

  @Mock
  private CodeChunkRepository codeChunkRepository;

  @Mock
  private QueryLogRepository queryLogRepository;

  @Mock
  private RepoRepository repoRepository;

  @Mock
  private EmbeddingClient embeddingClient;

  @Mock
  private LLMClient llmClient;

  @InjectMocks
  private RagQueryService ragQueryService;

  private Repo testRepo;
  private CodeChunk testChunk;

  @BeforeEach
  void setUp() {
    ragQueryService = new RagQueryService(
        codeChunkRepository,
        queryLogRepository,
        repoRepository,
        embeddingClient,
        llmClient,
        new ObjectMapper()
    );

    testRepo = Repo.builder()
        .id(1L)
        .owner("test-owner")
        .name("test-repo")
        .url("https://github.com/test-owner/test-repo")
        .status("READY")
        .build();

    testChunk = CodeChunk.builder()
        .id(1L)
        .repoId(1L)
        .filePath("src/Main.java")
        .chunkText("public class Main { ... }")
        .startLine(1)
        .endLine(10)
        .chunkType("CODE")
        .build();
  }

  @Test
  void testQuerySuccess() throws Exception {
    String question = "What is the main class?";
    String expectedAnswer = "The main class is at src/Main.java";

    when(repoRepository.findById(1L)).thenReturn(Optional.of(testRepo));
    when(embeddingClient.embed(question)).thenReturn(new Vector(new float[768]));
    when(codeChunkRepository.findSimilarChunks(eq(1L), anyString(), eq(5)))
        .thenReturn(List.of(testChunk));
    when(llmClient.chat(anyString())).thenReturn(expectedAnswer);

    var response = ragQueryService.query(1L, question);

    assertNotNull(response);
    assertEquals(expectedAnswer, response.getAnswer());
    assertEquals(1, response.getSources().size());
    assertEquals("src/Main.java", response.getSources().get(0).getFilePath());

    verify(queryLogRepository, times(1)).save(any());
  }

  @Test
  void testQueryNotReady() throws Exception {
    testRepo.setStatus("INDEXING");

    when(repoRepository.findById(1L)).thenReturn(Optional.of(testRepo));

    assertThrows(RuntimeException.class, () -> ragQueryService.query(1L, "test"));
  }

  @Test
  void testQueryRepoNotFound() {
    when(repoRepository.findById(999L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> ragQueryService.query(999L, "test"));
  }
}
