package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "devices")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_uid", nullable = false, unique = true)
    private String deviceUid;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "active")
    private boolean active = true;

    public Device(String deviceUid, String deviceName, String deviceModel, boolean active) {
        this.deviceUid = deviceUid;
        this.deviceName = deviceName;
        this.deviceModel = deviceModel;
        this.active = active;
    }

    // ✅ JSON dönüşünde "type" alanı olarak gösterilecek
    @Transient
    @JsonProperty("type")
    public String getType() {
        if (deviceModel == null) return null;

        String model = deviceModel.toUpperCase();

        if (model.contains("LED")) return "LED";
        if (model.contains("SERVO")) return "SERVO";
        if (model.contains("RFID")) return "RFID";
        if (model.contains("BUZZER")) return "BUZZER";
        if (model.contains("DHT")) return "DHT11";

        return "UNKNOWN";
    }
}
