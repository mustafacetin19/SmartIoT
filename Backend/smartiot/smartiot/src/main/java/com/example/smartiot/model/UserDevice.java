package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({
        "id", "assignedName", "status", "active",
        "user", "device", "userRoom",
        "deviceId", "deviceModel", "deviceName"
})
@Schema(description = "Kullanıcıya atanmış fiziki cihaz (mülkiyet/erişim kaydı)")
public class UserDevice {

    @Schema(description = "Atama durumu", enumAsRef = true)
    public enum Status { ACTIVE, INACTIVE_RESERVED, RETIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Veritabanı kimliği", example = "512")
    private Long id;

    /** users.id */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @Schema(description = "Sahip kullanıcı")
    private User user;

    /** devices.id */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @Schema(description = "Atanmış fiziki cihaz")
    private Device device;

    /** user_room.id */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_room_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @Schema(description = "Cihazın bulunduğu oda")
    private UserRoom userRoom;

    @Column(name = "assigned_name")
    @Schema(description = "Kullanıcının verdiği takma ad", example = "Salon Lamba 1")
    private String assignedName;

    @Column(name = "active")
    @Schema(description = "Kayıt aktif mi? (yumuşak silme için)", example = "true", defaultValue = "true")
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Schema(description = "Durum", example = "ACTIVE", allowableValues = { "ACTIVE", "INACTIVE_RESERVED", "RETIRED" })
    private Status status = Status.ACTIVE;

    // ---- JSON kolay alanlar (salt-okunur, frontend’e yardımcı) ----

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Fiziksel cihaz ID (device.id)", example = "601", accessMode = Schema.AccessMode.READ_ONLY)
    public Long getDeviceId() {
        return device != null ? device.getId() : null;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Cihaz modeli (device.deviceModel)", example = "Led-White", accessMode = Schema.AccessMode.READ_ONLY)
    public String getDeviceModel() {
        return device != null ? device.getDeviceModel() : null;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Cihaz katalog adı (device.deviceName)", example = "White LED", accessMode = Schema.AccessMode.READ_ONLY)
    public String getDeviceName() {
        return device != null ? device.getDeviceName() : null;
    }
}
