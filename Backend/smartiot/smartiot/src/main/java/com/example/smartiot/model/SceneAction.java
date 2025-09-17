package com.example.smartiot.model;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Entity
@Table(name = "scene_actions")
public class SceneAction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(nullable = false, length = 32)
    private String action; // ON/OFF/SET_LEVEL/OPEN/CLOSE/STOP

    @Column(length = 64)
    private String value;  // örn: "60"

    @Column(name = "sort_order")
    private Integer sortOrder = 0;
}
