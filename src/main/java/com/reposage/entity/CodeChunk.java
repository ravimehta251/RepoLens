package com.reposage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.pgvector.hibernate.Vector;
import java.time.LocalDateTime;

@Entity
@Table(name = "code_chunk", indexes = {
    @Index(name = "idx_repo_id", columnList = "repo_id"),
    @Index(name = "idx_embedding", columnList = "embedding", 
            options = "USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100)")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeChunk {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long repoId;

  @Column(nullable = false, length = 1000)
  private String filePath;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String chunkText;

  @Column(columnDefinition = "vector(768)")
  private Vector embedding;

  private Integer startLine;

  private Integer endLine;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String chunkType = "CODE"; // README / CODE / COMMENT

  @Column(nullable = false, name = "created_at", updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
