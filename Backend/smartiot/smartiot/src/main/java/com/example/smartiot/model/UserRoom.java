package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_room",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_name"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // users.id ile ilişki
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(nullable = false)
    private Boolean active = true;
}
