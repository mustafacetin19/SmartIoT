package com.example.smartiot.model;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sistemde oturum açan son kullanıcı")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Veritabanı kimliği", example = "7")
    private Long id;

    @Schema(description = "E-posta adresi", example = "user@example.com")
    private String email;

    @Schema(description = "Şifre (örnek amaçlı düz metin; prod için hash önerilir)",
            example = "123456")
    private String password;

    @Column(name = "first_name")
    @Schema(description = "Ad", example = "Mustafa")
    private String firstName;

    @Column(name = "last_name")
    @Schema(description = "Soyad", example = "Çetin")
    private String lastName;

    @Column(name = "active")
    @Schema(description = "Kullanıcı aktif mi?", example = "true", defaultValue = "true")
    private boolean active = true;  // varsayılan: aktif
}
