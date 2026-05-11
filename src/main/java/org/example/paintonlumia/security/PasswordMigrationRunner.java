package org.example.paintonlumia.security;

import org.example.paintonlumia.entity.UserEntity;
import org.example.paintonlumia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("========== 비밀번호 마이그레이션 검사 시작 ==========");

        List<UserEntity> allUsers = userRepository.findAll();
        int migratedCount = 0;

        for (UserEntity user : allUsers) {
            String currentPassword = user.getPassword();

            // 만약 비밀번호가 null 이거나 비어있으면 건너뜀
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                continue;
            }

            // BCrypt 해시는 항상 "$2a$" 형태 등으로 시작합니다.
            // 이 기호로 시작하지 않는다면 아직 평문 비밀번호라고 간주하고 변환합니다.
            if (!currentPassword.startsWith("$2a$") && !currentPassword.startsWith("$2b$") && !currentPassword.startsWith("$2y$")) {
                log.info("아이디 [{}] 의 평문 비밀번호를 BCrypt로 암호화합니다.", user.getUsername());

                String encodedPassword = passwordEncoder.encode(currentPassword);
                user.setPassword(encodedPassword);

                migratedCount++;
            }
        }

        if (migratedCount > 0) {
            userRepository.saveAll(allUsers);
            log.info("========== 총 {} 명의 비밀번호가 안전하게 암호화되었습니다. ==========", migratedCount);
        } else {
            log.info("========== 변환할 평문 비밀번호가 없습니다. ==========");
        }
    }
}