// src/main/java/org/example/paintonlumia/controller/AuthController.java
package org.example.paintonlumia.controller;

import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private static final long REGEN_TIME_MS = 10000; // 10초

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (userRepository.existsById(username)) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        long now = System.currentTimeMillis();
        // 인자 6개 생성자 사용 (password 포함)
        UserEntity newUser = new UserEntity(username, password, 100, 100, 0, now);
        userRepository.save(newUser);

        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        UserEntity user = userRepository.findById(username).orElse(null);
        if (user == null || !user.getPassword().equals(password)) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // --- 오프라인 쿼터 소급 연산 로직 ---
        long now = System.currentTimeMillis();

        if (user.getCurrentQuota() < user.getMaxQuota()) {
            long elapsed = now - user.getLastUpdateTime();
            int generatedQuotas = (int) (elapsed / REGEN_TIME_MS);

            if (generatedQuotas > 0) {
                int newQuota = Math.min(user.getMaxQuota(), user.getCurrentQuota() + generatedQuotas);
                user.setCurrentQuota(newQuota);

                long remainder = elapsed % REGEN_TIME_MS;
                user.setLastUpdateTime(now - remainder);
            }
        } else {
            user.setLastUpdateTime(now);
        }

        userRepository.save(user); // 충전된 쿼터 DB 저장
        return ResponseEntity.ok(user);
    }
}