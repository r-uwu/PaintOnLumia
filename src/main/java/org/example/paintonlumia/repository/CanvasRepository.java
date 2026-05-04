package org.example.paintonlumia.repository;

import org.example.paintonlumia.dto.PixelMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct; // Spring Boot 3.x 기준
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CanvasRepository {

    private static final String CANVAS_KEY = "canvas:state";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, String> hashOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
    }

    // 에러 발생 원인: 이 메서드가 이 클래스 안에 있어야 합니다.
    public void savePixel(PixelMessage msg) {
        int offset = msg.getSize() / 2;
        Map<String, String> pixelBatch = new HashMap<>();

        for (int i = 0; i < msg.getSize(); i++) {
            for (int j = 0; j < msg.getSize(); j++) {
                int px = (msg.getX() - offset) + i;
                int py = (msg.getY() - offset) + j;

                if (px >= 0 && px < 2000 && py >= 0 && py < 2000) {
                    String field = px + ":" + py;
                    pixelBatch.put(field, msg.getColor());
                }
            }
        }
        hashOps.putAll(CANVAS_KEY, pixelBatch);
    }

    // 에러 발생 원인: 이 메서드 역시 이 클래스 안에 있어야 합니다.
    public Map<String, String> getCanvasSnapshot() {
        return hashOps.entries(CANVAS_KEY);
    }
}