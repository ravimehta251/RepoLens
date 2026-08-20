package com.reposage.repository;

import com.reposage.entity.CodeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.pgvector.hibernate.Vector;

import java.util.List;

@Repository
public interface CodeChunkRepository extends JpaRepository<CodeChunk, Long> {
  List<CodeChunk> findByRepoId(Long repoId);

  @Query(value = "SELECT * FROM code_chunk " +
      "WHERE repo_id = :repoId " +
      "ORDER BY embedding <=> :queryEmbedding::vector " +
      "LIMIT :k", nativeQuery = true)
  List<CodeChunk> findSimilarChunks(
      @Param("repoId") Long repoId,
      @Param("queryEmbedding") String queryEmbeddingString,
      @Param("k") int k
  );

  long countByRepoId(Long repoId);
}
