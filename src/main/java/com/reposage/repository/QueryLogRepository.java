package com.reposage.repository;

import com.reposage.entity.QueryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {
  List<QueryLog> findByRepoId(Long repoId);
}
