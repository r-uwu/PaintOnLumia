package org.example.paintonlumia.repository;

import org.example.paintonlumia.entity.CanvasArchiveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanvasArchiveRepository extends JpaRepository<CanvasArchiveEntity, Long> {
}