package com.reposage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "query_log", indexes = @Index(name = "idx_repo_id", columnList = "repo_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long repoId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String question;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String answer;

  @Column(nullable = false, columnDefinition = "jsonb")
  private String sourceFiles; // JSON array of {file_path, start_line, end_line}

  @Column(nullable = false, name = "created_at", updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
