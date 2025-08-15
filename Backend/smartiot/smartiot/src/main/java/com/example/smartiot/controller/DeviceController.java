package com.example.smartiot.controller;

import com.example.smartiot.service.MqttPublisher;
import com.example.smartiot.service.TelemetryService;
import com.example.smartiot.service.UserDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Device Control", description = "Cihaz kontrol ve telemetri uçları (yetki kontrollü)")
@RestController
@RequestMapping("/api/control")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DeviceController {

    private final UserDeviceService userDeviceService;
    private final MqttPublisher mqttPublisher;
    private final TelemetryService telemetryService;

    // ===== LED =====
    @Operation(summary = "LED aç/kapat (yetki kontrollü)")
    @ApiResponse(responseCode = "200", description = "Komut gönderildi")
    @PostMapping("/led")
    public ResponseEntity<?> ledToggle(
            @Parameter(description = "Kullanıcı ID", example = "7") @RequestParam Long userId,
            @Parameter(description = "Device ID", example = "210") @RequestParam Long deviceId,
            @Parameter(description = "Durum", example = "true") @RequestParam boolean state) {

        if (!userDeviceService.userHasAccess(userId, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this device");
        }
        mqttPublisher.publishLedByDeviceId(deviceId, state);
        return ResponseEntity.ok().build();
    }

    // ===== BUZZER =====
    @Operation(summary = "Buzzer (yetki kontrollü)")
    @ApiResponse(responseCode = "200", description = "Komut gönderildi")
    @PostMapping("/buzzer")
    public ResponseEntity<?> buzzer(
            @RequestParam Long userId,
            @RequestParam Long deviceId,
            @RequestParam(defaultValue = "beep") String action) {

        if (!userDeviceService.userHasAccess(userId, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this device");
        }
        mqttPublisher.publishBuzzerByDeviceId(deviceId, action);
        return ResponseEntity.ok().build();
    }

    // ===== SERVO =====
    @Operation(summary = "Servo açı ver (yetki kontrollü)")
    @ApiResponse(responseCode = "200", description = "Komut gönderildi")
    @PostMapping("/servo")
    public ResponseEntity<?> servo(
            @RequestParam Long userId,
            @RequestParam Long deviceId,
            @RequestParam int angle) {

        if (!userDeviceService.userHasAccess(userId, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this device");
        }
        mqttPublisher.publishServoByDeviceId(deviceId, angle);
        return ResponseEntity.ok().build();
    }

    // ====== DHT11 (Sensor) - son değer (yetki kontrollü okuma) ======
    @Operation(summary = "DHT11 son değer (yetki kontrollü)")
    @GetMapping("/sensor/last")
    public ResponseEntity<?> sensorLast(
            @RequestParam Long userId,
            @RequestParam Long deviceId) {

        if (!userDeviceService.userHasAccess(userId, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this device");
        }
        var reading = telemetryService.getSensor(deviceId);
        if (reading == null) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(reading);
    }

    // ====== RFID - son kart (yetki kontrollü okuma) ======
    @Operation(summary = "RFID son kart (yetki kontrollü)")
    @GetMapping("/rfid/last")
    public ResponseEntity<?> rfidLast(
            @RequestParam Long userId,
            @RequestParam Long deviceId) {

        if (!userDeviceService.userHasAccess(userId, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authorized for this device");
        }
        var last = telemetryService.getRfid(deviceId);
        if (last == null) return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        return ResponseEntity.ok(new RfidDto(last));
    }

    // ==== (Opsiyonel) JSON body ile de kabul eden DTO'lar ====
    @Data
    static class ServoReq { private Long userId; private Long deviceId; private int angle; }
    @Data
    static class LedReq   { private Long userId; private Long deviceId; private boolean state; }
    @Data
    static class BuzzReq  { private Long userId; private Long deviceId; private String action; }
    public record RfidDto(String cardId) {}
}
