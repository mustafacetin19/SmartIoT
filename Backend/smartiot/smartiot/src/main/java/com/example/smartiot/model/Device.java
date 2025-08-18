package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({ "id", "deviceUid", "deviceName", "deviceModel", "type", "active" })
@Schema(description = "Sistemde kayıtlı donanım cihazı")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Veritabanı kimliği", example = "101")
    private Long id;

    @Column(name = "device_uid", nullable = false, unique = true)
    @Schema(description = "Cihazın benzersiz donanım UID'i", example = "LEDW-000123")
    private String deviceUid;

    @Column(name = "device_name")
    @Schema(description = "Görünen ad (katalog adı, alias değil)", example = "White LED")
    private String deviceName;

    @Column(name = "device_model")
    @Schema(description = "Cihazın model/türü", example = "Led-White")
    private String deviceModel;

    @Column(name = "active")
    @Schema(description = "Cihaz aktif mi?", example = "true", defaultValue = "true")
    private boolean active = true;

    public Device(String deviceUid, String deviceName, String deviceModel, boolean active) {
        this.deviceUid = deviceUid;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.active = active;
    }

    /** JSON dönüşünde sadece-okunur 'type' alanı olarak gösterilir. */
    @Transient
    @JsonProperty("type")
    @Schema(
            description = "Modelden türetilen cihaz tipi",
            accessMode = Schema.AccessMode.READ_ONLY,
            example = "LED",
            allowableValues = { "LED", "SERVO", "RFID", "BUZZER", "DHT11", "UNKNOWN" }
    )
    public String getType() {
        if (deviceModel == null) return null;
        String model = deviceModel.toUpperCase();
        if (model.contains("LED"))   return "LED";
        if (model.contains("SERVO")) return "SERVO";
        if (model.contains("RFID"))  return "RFID";
        if (model.contains("BUZZ"))  return "BUZZER";
        if (model.contains("DHT"))   return "DHT11";
        return "UNKNOWN";
    }
}
