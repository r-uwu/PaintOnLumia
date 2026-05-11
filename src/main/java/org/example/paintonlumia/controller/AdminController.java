package org.example.paintonlumia.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paintonlumia.entity.CanvasArchiveEntity;
import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.CanvasArchiveRepository;
import org.example.paintonlumia.repository.CanvasRepository;
import org.example.paintonlumia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CanvasRepository canvasRepository;
    private final CanvasArchiveRepository archiveRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/archive")
    public ResponseEntity<?> createArchive(@RequestParam String username, @RequestParam String archiveName) {
        // 1. 관리자 권한 검증
        UserEntity user = userRepository.findById(username).orElse(null);
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body("관리자 권한이 없습니다.");
        }

        try {
            // 2. 현재 Redis의 캔버스 데이터 조회
            Map<String, String> currentCanvas = canvasRepository.getCanvasSnapshot();

            // 3. 데이터를 JSON 문자열로 압축(직렬화)
            String snapshotJson = objectMapper.writeValueAsString(currentCanvas);

            // 4. 아카이브 DB에 저장
            CanvasArchiveEntity archive = new CanvasArchiveEntity();
            archive.setArchiveName(archiveName);
            archive.setArchivedBy(username);
            archive.setSnapshotData(snapshotJson);
            archive.setCreatedAt(System.currentTimeMillis());

            archiveRepository.save(archive);

            return ResponseEntity.ok("캔버스 아카이브 [" + archiveName + "] 저장이 완료되었습니다.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("아카이브 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}