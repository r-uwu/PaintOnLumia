// src/main/java/org/example/paintonlumia/controller/UserController.java
package org.example.paintonlumia.controller;

import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * 동기화 전용으로 축소, 기능 authController
 *
 **/
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/info")
    public ResponseEntity<UserEntity> getUserInfo(@RequestParam String username) {
        UserEntity user = userRepository.findById(username).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    // 클라이언트의 현재 상태를 DB에 덮어쓰기 (5초 주기 호출용)
    @PostMapping("/sync")
    public void syncState(@RequestBody UserEntity clientState) {
        UserEntity existingUser = userRepository.findById(clientState.getUsername()).orElse(null);

        if (existingUser != null) {
            // 프론트엔드에서 비밀번호를 보내지 않으므로, DB에 있던 기존 비밀번호를 유지시킴
            clientState.setPassword(existingUser.getPassword());
            clientState.setLastUpdateTime(System.currentTimeMillis());

            userRepository.save(clientState);
        }
    }
}