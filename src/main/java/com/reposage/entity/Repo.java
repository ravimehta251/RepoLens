package com.reposage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "repo", uniqueConstraints = @UniqueConstraint(columnNames = "url"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String owner;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, length = 500, unique = true)
  private String url;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "PENDING"; // PENDING / INDEXING / READY / FAILED

  @Column(name = "indexed_at")
  private LocalDateTime indexedAt;

  @Column(nullable = false, name = "created_at", updatable = false)
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}
