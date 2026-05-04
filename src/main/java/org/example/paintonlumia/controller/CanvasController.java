package org.example.paintonlumia.controller;

import lombok.RequiredArgsConstructor;
import org.example.paintonlumia.dto.PixelMessage;
import org.example.paintonlumia.repository.CanvasRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CanvasController {

    private final CanvasRepository canvasRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/draw")
    public void handleDraw(PixelMessage message) {
        // 1. Save to Redis
        canvasRepository.savePixel(message);

        // 2. Broadcast to all clients subscribed to /topic/canvas
        messagingTemplate.convertAndSend("/topic/canvas", message);
    }

    // REST API for new clients to download the entire canvas state upon connection
    @GetMapping("/api/canvas/snapshot")
    public Map<String, String> getSnapshot() {
        return canvasRepository.getCanvasSnapshot();
    }
}