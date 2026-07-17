package org.example.paintonlumia.controller;

import org.example.paintonlumia.dto.ChunkImportRequest;
import org.example.paintonlumia.service.CanvasArchiveService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CanvasArchiveService archiveService;

    public AdminController(CanvasArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping("/archive")
    public ResponseEntity<String> createArchive(@RequestParam String username, @RequestParam String archiveName) {
        try {
            archiveService.createArchive(archiveName, username);
            return ResponseEntity.ok("아카이브가 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/archives")
    public ResponseEntity<List<String>> getArchives() {
        return ResponseEntity.ok(archiveService.getAllArchiveNames());
    }

    @PostMapping("/import-chunk")
    public ResponseEntity<String> importChunk(@RequestBody ChunkImportRequest request) {
        try {
            archiveService.importChunkToLiveCanvas(request);
            return ResponseEntity.ok("지정된 영역의 데이터가 성공적으로 복원되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}