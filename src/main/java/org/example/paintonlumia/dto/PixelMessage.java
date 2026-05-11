package org.example.paintonlumia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PixelMessage {
    private int x;
    private int y;
    private String color;
    private int size;
    private String author;
    private String title;
    private String badges;
    private String profile; //Base64이미지로
}