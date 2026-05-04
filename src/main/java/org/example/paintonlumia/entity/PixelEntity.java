package org.example.paintonlumia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "canvas_pixels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PixelEntity {

    // "x:y" 형태의 좌표 문자열을 Primary Key로 사용 (업데이트 용이성)
    @Id
    private String coordinateKey;

    private String color;
}