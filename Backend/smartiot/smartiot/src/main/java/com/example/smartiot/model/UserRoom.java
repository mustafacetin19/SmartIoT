package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "user_room",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "room_name" })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kullanıcının odası")
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Veritabanı kimliği", example = "11")
    private Long id;

    /** users.id ile ilişki */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @Schema(description = "Odayı oluşturan kullanıcı")
    private User user;

    @Column(name = "room_name", nullable = false)
    @Schema(description = "Oda adı (kullanıcıya özgü)", example = "Salon")
    private String roomName;

    @Column(nullable = false)
    @Schema(description = "Oda aktif mi?", example = "true", defaultValue = "true")
    private Boolean active = true;
}
