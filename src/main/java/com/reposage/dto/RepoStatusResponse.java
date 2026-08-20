package com.reposage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepoStatusResponse {
  private Long repoId;
  private String owner;
  private String name;
  private String status;
  private Integer chunkCount;
}
