package org.example.paintonlumia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "canvas_archives")
@Getter
@NoArgsConstructor
public class CanvasArchiveEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String archiveName;

    @Column(nullable = false)
    private String archivedBy;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String snapshotData;

    @Column(nullable = false)
    private long createdAt;

    // 초기화 및 불변성 유지를 위한 생성자
    public CanvasArchiveEntity(String archiveName, String snapshotData, String archivedBy) {
        this.archiveName = archiveName;
        this.snapshotData = snapshotData;
        this.archivedBy = archivedBy;
        this.createdAt = System.currentTimeMillis();
    }
}