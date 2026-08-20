package com.reposage.repository;

import com.reposage.entity.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {
  Optional<Repo> findByUrl(String url);
  Optional<Repo> findByOwnerAndName(String owner, String name);
}
