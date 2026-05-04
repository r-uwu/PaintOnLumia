package org.example.paintonlumia.repository;

import org.example.paintonlumia.entity.PixelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PixelJpaRepository extends JpaRepository<PixelEntity, String> {
}