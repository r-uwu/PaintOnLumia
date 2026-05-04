package org.example.paintonlumia.service;

import org.example.paintonlumia.entity.PixelEntity;
import org.example.paintonlumia.repository.CanvasRepository;
import org.example.paintonlumia.repository.PixelJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanvasBackupService {

    private final CanvasRepository redisRepository;
    private final PixelJpaRepository mysqlRepository;

    // 1분(60000ms)마다 백업 실행
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void backupRedisToMysql() {
        log.info("Redis -> MySQL 캔버스 백업 시작...");

        // 1. Redis에서 현재 그려진 모든 픽셀 데이터 가져오기
        Map<String, String> snapshot = redisRepository.getCanvasSnapshot();

        if (snapshot.isEmpty()) {
            log.info("저장할 픽셀 데이터가 없습니다.");
            return;
        }

        // 2. Map 데이터를 JPA Entity 리스트로 변환
        List<PixelEntity> entitiesToSave = snapshot.entrySet().stream()
                .map(entry -> new PixelEntity(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 3. MySQL에 일괄 저장 (이미 존재하는 좌표키면 UPDATE, 없으면 INSERT)
        mysqlRepository.saveAll(entitiesToSave);

        log.info("백업 완료: 총 {} 개의 픽셀 데이터 저장됨", entitiesToSave.size());
    }
}