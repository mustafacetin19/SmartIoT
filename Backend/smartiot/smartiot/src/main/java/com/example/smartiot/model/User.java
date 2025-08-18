package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sistemde oturum açan son kullanıcı")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Veritabanı kimliği", example = "7")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "E-posta adresi", example = "user@example.com")
    private String email;

    @Schema(
            description = "Şifre (örnek: demoda düz metin; prod'da hashlenmeli)",
            example = "123456",
            accessMode = Schema.AccessMode.WRITE_ONLY
    )
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // response'ta gizle, request'te kabul et
    private String password;

    @Column(name = "first_name")
    @Schema(description = "Ad", example = "Mustafa")
    private String firstName;

    @Column(name = "last_name")
    @Schema(description = "Soyad", example = "Çetin")
    private String lastName;

    @Column(name = "active")
    @Schema(description = "Kullanıcı aktif mi?", example = "true", defaultValue = "true")
    private boolean active = true;
}
