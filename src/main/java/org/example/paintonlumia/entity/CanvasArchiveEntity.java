package org.example.paintonlumia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "canvas_archives")
@Getter
@Setter
@NoArgsConstructor
public class CanvasArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String archiveName;

    private String archivedBy;

    @Column(columnDefinition = "LONGTEXT")
    private String snapshotData; // 캔버스 전체 데이터 (JSON 형태)

    private long createdAt;
}