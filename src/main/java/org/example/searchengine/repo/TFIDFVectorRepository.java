package org.example.searchengine.repo;

import org.example.searchengine.models.TFIDFVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TFIDFVectorRepository extends JpaRepository<TFIDFVector , Long> {
}
