package org.example.paintonlumia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    private String username;
    private String password;
    private String nickname;

    @Column(nullable = false)
    private String role = "USER";

    // 픽셀 프로필 이미지 (Base64 인코딩 저장용)
    @Column(columnDefinition = "LONGTEXT")
    private String profileIconBase64;

    @Column(columnDefinition = "TEXT")
    private String unlockedColors;

    // 즐찾색상
    @Column(columnDefinition = "TEXT")
    private String favoriteColors;

    private int currentQuota;
    private int maxQuota;
    private int points;
    private long lastUpdateTime;

}