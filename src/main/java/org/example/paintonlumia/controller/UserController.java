// src/main/java/org/example/paintonlumia/controller/UserController.java
package org.example.paintonlumia.controller;

import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            // 객체로 덮어쓰지 않고 필요한 값만 업데이트하게 변경,,,
            existingUser.setCurrentQuota(clientState.getCurrentQuota());
            existingUser.setMaxQuota(clientState.getMaxQuota());
            existingUser.setPoints(clientState.getPoints());
            existingUser.setUnlockedColors(clientState.getUnlockedColors());
            existingUser.setFavoriteColors(clientState.getFavoriteColors());
            existingUser.setLastUpdateTime(System.currentTimeMillis());
            existingUser.setUnlockedBadges(clientState.getUnlockedBadges());
            existingUser.setLastUpdateTime(System.currentTimeMillis());

            userRepository.save(existingUser);
        }
    }

    // ✨ 3. 프로필 및 설정 업데이트 API (이미지 + 텍스트 정보 통합 저장)
    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        UserEntity user = userRepository.findById(username).orElse(null);

        if (user == null) {
            return ResponseEntity.badRequest().body("유저를 찾을 수 없습니다.");
        }

        // 1. 프로필 이미지 업데이트 (데이터가 있을 때만)
        if (payload.containsKey("image") && payload.get("image") != null && !payload.get("image").isEmpty()) {
            user.setProfileIconBase64(payload.get("image"));
        }

        // 2. 텍스트 정보 업데이트
        if (payload.containsKey("nickname")) user.setNickname(payload.get("nickname"));
        if (payload.containsKey("title")) user.setTitle(payload.get("title"));
        if (payload.containsKey("badges")) user.setBadges(payload.get("badges"));

        userRepository.save(user);

        return ResponseEntity.ok("프로필이 성공적으로 업데이트되었습니다.");
    }

}