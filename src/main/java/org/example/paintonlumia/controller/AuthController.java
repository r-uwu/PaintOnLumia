// src/main/java/org/example/paintonlumia/controller/AuthController.java
package org.example.paintonlumia.controller;

import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.UserRepository;
import org.example.paintonlumia.security.JwtTokenProvider; // ✨ JWT 토큰 제공자 Import
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // ✨ 비밀번호 암호화 도구 Import
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    // ✨ 누락되었던 보안 클래스들을 주입(Injection) 받습니다.
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    private static final long REGEN_TIME_MS = 10000; // 10초

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        if (userRepository.existsById(username)) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        UserEntity newUser = new UserEntity();
        newUser.setUsername(username);

        // ✨ 평문 비밀번호 대신 BCrypt로 안전하게 암호화하여 저장합니다.
        newUser.setPassword(passwordEncoder.encode(password));

        newUser.setNickname("유저_" + (int)(Math.random() * 1000));
        newUser.setUnlockedColors("#000000,#1A1A1A,#333333,#4D4D4D,#666666,#808080,#B3B3B3,#CCCCCC,#E6E6E6,#FFFFFF,#FF0000,#FF4500");
        newUser.setFavoriteColors("");
        newUser.setCurrentQuota(300);
        newUser.setMaxQuota(300);
        newUser.setPoints(0);
        newUser.setLastUpdateTime(System.currentTimeMillis());
        newUser.setRole("USER"); // ✨ JWT 토큰에 넣기 위해 권한(Role) 필수 지정

        userRepository.save(newUser);

        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        UserEntity user = userRepository.findById(username).orElse(null);

        // ✨ equals 대신 passwordEncoder.matches()를 사용하여 암호화된 비밀번호를 비교합니다.
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(401).body("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // --- 훌륭하게 작성하신 오프라인 쿼터 소급 연산 로직 (유지) ---
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

        // ✨ 쿼터가 소급 정산되었으므로 변경된 정보를 DB에 한 번 저장해 줍니다.
        userRepository.save(user);

        // ✨ JWT 발급 로직
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("username", user.getUsername());

        return ResponseEntity.ok(response);
    }
}