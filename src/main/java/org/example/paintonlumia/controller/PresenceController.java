// src/main/java/org/example/paintonlumia/controller/PresenceController.java
package org.example.paintonlumia.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
public class PresenceController {

    private final SimpMessagingTemplate messagingTemplate;
    // Thread-safe set for concurrent access
    private final Set<String> activeUsers = ConcurrentHashMap.newKeySet();

    @MessageMapping("/join")
    public void join(String username) {
        activeUsers.add(username);
        messagingTemplate.convertAndSend("/topic/users", activeUsers);
    }

    @MessageMapping("/leave")
    public void leave(String username) {
        activeUsers.remove(username);
        messagingTemplate.convertAndSend("/topic/users", activeUsers);
    }
}