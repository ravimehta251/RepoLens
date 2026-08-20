package com.reposage.service;

import com.reposage.entity.CodeChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ChunkService {
  @Value("${rag.chunk-size:500}")
  private int chunkSize;

  @Value("${rag.chunk-overlap:50}")
  private int chunkOverlap;

  public List<CodeChunk> chunkFile(String filePath, String content, Long repoId) {
    List<CodeChunk> chunks = new ArrayList<>();
    
    String chunkType = determineChunkType(filePath);
    List<String> lines = Arrays.asList(content.split("\n"));
    
    int currentPos = 0;
    int chunkIndex = 0;

    while (currentPos < content.length()) {
      int endPos = Math.min(currentPos + chunkSize, content.length());
      
      // Try to break at word boundary if not at end
      if (endPos < content.length()) {
        int lastSpace = content.lastIndexOf(" ", endPos);
        if (lastSpace > currentPos + chunkSize / 2) {
          endPos = lastSpace;
        }
      }

      String chunkText = content.substring(currentPos, endPos).trim();
      if (!chunkText.isEmpty()) {
        int startLine = countLines(content, 0, currentPos);
        int endLine = countLines(content, 0, endPos);

        CodeChunk chunk = CodeChunk.builder()
            .repoId(repoId)
            .filePath(filePath)
            .chunkText(chunkText)
            .startLine(startLine)
            .endLine(endLine)
            .chunkType(chunkType)
            .build();
        chunks.add(chunk);
      }

      currentPos = endPos - (chunkIndex > 0 ? chunkOverlap : 0);
      chunkIndex++;
    }

    log.info("Chunked {} into {} chunks", filePath, chunks.size());
    return chunks;
  }

  private String determineChunkType(String filePath) {
    if (filePath.contains("README") || filePath.endsWith(".md")) {
      return "README";
    }
    return "CODE";
  }

  private int countLines(String content, int start, int end) {
    return (int) content.substring(0, end).lines().count();
  }
}
