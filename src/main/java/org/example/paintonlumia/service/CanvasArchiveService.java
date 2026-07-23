package org.example.paintonlumia.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paintonlumia.dto.ChunkImportRequest;
import org.example.paintonlumia.entity.CanvasArchiveEntity;
import org.example.paintonlumia.repository.CanvasArchiveRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CanvasArchiveService {

    private final CanvasArchiveRepository archiveRepository; //
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String REDIS_CANVAS_KEY = "canvas:live";

    public CanvasArchiveService(CanvasArchiveRepository archiveRepository,
                                RedisTemplate<String, String> redisTemplate,
                                ObjectMapper objectMapper) {
        this.archiveRepository = archiveRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    // 현재 Redis에 있는 캔버스 데이터를 아카이브 엔티티로 저장
    @Transactional
    public void createArchive(String archiveName, String username) throws Exception {
        Map<Object, Object> liveData = redisTemplate.opsForHash().entries(REDIS_CANVAS_KEY);
        String snapshotJson = objectMapper.writeValueAsString(liveData);

        CanvasArchiveEntity archive = new CanvasArchiveEntity(archiveName, snapshotJson, username);
        archiveRepository.save(archive); //[cite: 1]
    }

    // 데이터베이스에 저장된 모든 아카이브의 이름을 반환
    @Transactional(readOnly = true)
    public List<String> getAllArchiveNames() {
        return archiveRepository.findAll().stream() //[cite: 1]
                .map(CanvasArchiveEntity::getArchiveName)
                .collect(Collectors.toList());
    }

    // 지정된 아카이브에서 특정 영역의 픽셀을 추출하여 현재 라이브 Redis 데이터에 덮어쓰기
    @Transactional
    public void importChunkToLiveCanvas(ChunkImportRequest request) throws Exception {
        CanvasArchiveEntity archive = archiveRepository.findAll().stream() //[cite: 1]
                .filter(a -> a.getArchiveName().equals(request.getArchiveName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("아카이브를 찾을 수 없습니다."));

        // JSON 스냅샷 데이터를 Map으로 역직렬화
        Map<String, String> snapshotData = objectMapper.readValue(
                archive.getSnapshotData(),
                new TypeReference<Map<String, String>>() {}
        );

        int mapSize = 2000;
        int targetOffsetX = request.getTargetX() - request.getStartX();
        int targetOffsetY = request.getTargetY() - request.getStartY();

        // 파이프라인 처리를 위한 설정은 생략하고 해시맵에 일괄 업데이트 진행
        for (int x = request.getStartX(); x <= request.getEndX(); x++) {
            for (int y = request.getStartY(); y <= request.getEndY(); y++) {
                // 모듈러 연산을 통해 무한 맵 좌표계 보정
                int wX = ((x % mapSize) + mapSize) % mapSize;
                int wY = ((y % mapSize) + mapSize) % mapSize;
                String sourceKey = wX + ":" + wY;

                if (snapshotData.containsKey(sourceKey)) {
                    int newX = (((x + targetOffsetX) % mapSize) + mapSize) % mapSize;
                    int newY = (((y + targetOffsetY) % mapSize) + mapSize) % mapSize;
                    String targetKey = newX + ":" + newY;

                    // Redis 라이브 캔버스 데이터 덮어쓰기
                    redisTemplate.opsForHash().put(REDIS_CANVAS_KEY, targetKey, snapshotData.get(sourceKey));
                }
            }
        }
    }
}