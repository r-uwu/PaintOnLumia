package org.example.paintonlumia.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChunkImportRequest {
    private String archiveName;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int targetX;
    private int targetY;
}