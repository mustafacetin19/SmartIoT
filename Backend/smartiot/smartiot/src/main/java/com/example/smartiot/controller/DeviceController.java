package com.example.smartiot.controller;

import com.example.smartiot.model.ServoCommand;
import com.example.smartiot.service.MqttPublisher;
import com.example.smartiot.service.SensorDataService;
import com.example.smartiot.service.DeviceService;
import com.example.smartiot.model.Device;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Device", description = "Cihaz yönetimi ve kontrol uçları")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DeviceController {

    private final DeviceService deviceService;
    private final SensorDataService sensorDataService;
    private final MqttPublisher mqttPublisher;

    public DeviceController(DeviceService deviceService,
                            SensorDataService sensorDataService,
                            MqttPublisher mqttPublisher) {
        this.deviceService = deviceService;
        this.sensorDataService = sensorDataService;
        this.mqttPublisher = mqttPublisher;
    }

    @Operation(summary = "Tüm cihazları listele")
    @ApiResponse(responseCode = "200", description = "Cihaz listesi",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Device.class)),
                    examples = @ExampleObject(value = "[{\"id\":101,\"deviceUid\":\"ESP32-ABCD\",\"deviceName\":\"Salon Lamba 1\",\"deviceModel\":\"LED_WS2812B\",\"active\":true,\"type\":\"LED\"}]")))
    @GetMapping("/devices")
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @Operation(summary = "Aktif cihazları listele")
    @ApiResponse(responseCode = "200", description = "Aktif cihazlar",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Device.class))))
    @GetMapping("/devices/active")
    public List<Device> getActiveDevices() {
        return deviceService.getActiveDevices();
    }

    @Operation(summary = "Yeni cihaz ekle")
    @ApiResponse(responseCode = "200", description = "Oluşturulan cihaz",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Device.class),
                    examples = @ExampleObject(value = "{\"id\":102,\"deviceUid\":\"RFID-001\",\"deviceName\":\"Giriş RFID\",\"deviceModel\":\"RFID_RC522\",\"active\":true,\"type\":\"RFID\"}")))
    @PostMapping("/devices")
    public Device addDevice(
            @RequestBody(description = "Oluşturulacak cihaz",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Device.class),
                            examples = @ExampleObject(value = "{\"deviceUid\":\"ESP32-XYZ\",\"deviceName\":\"Yatak LED\",\"deviceModel\":\"LED_WS2812B\",\"active\":true}")))
            @org.springframework.web.bind.annotation.RequestBody Device device) {
        return deviceService.saveDevice(device);
    }

    // ==== Kontrol endpointleri ====

    @Operation(summary = "Servo 1 toggle (0° ↔ 90°)")
    @ApiResponse(responseCode = "200", description = "Komut gönderildi")
    @PostMapping("/control/servo1")
    public void toggleServo1() {
        sensorDataService.setServo1Open(!sensorDataService.isServo1Open());
        System.out.println("Servo 1 durumu güncellendi ➜ " +
                (sensorDataService.isServo1Open() ? "90 derece" : "0 derece"));
    }

    @Operation(summary = "Servo 2 toggle (0° ↔ 90°)")
    @ApiResponse(responseCode = "200", description = "Komut gönderildi")
    @PostMapping("/control/servo2")
    public void toggleServo2() {
        sensorDataService.setServo2Open(!sensorDataService.isServo2Open());
        System.out.println("Servo 2 durumu güncellendi ➜ " +
                (sensorDataService.isServo2Open() ? "90 derece" : "0 derece"));
    }

    @Operation(summary = "Genel LED toggle")
    @ApiResponse(responseCode = "200", description = "LED durumu değiştirildi")
    @PostMapping("/control/led")
    public void toggleLed() {
        sensorDataService.setLedOn(!sensorDataService.isLedOn());
        System.out.println("LED durumu: " + (sensorDataService.isLedOn() ? "AÇIK" : "KAPALI"));
    }

    @Operation(summary = "Renkli LED aç/kapat (MQTT)",
            description = "color: red/green/blue..., state: true/false")
    @ApiResponse(responseCode = "200", description = "LED komutu gönderildi",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "💡 LED komutu gönderildi: red_on")))
    @PostMapping("/led/toggle")
    public String toggleLed(
            @Parameter(description = "Renk", example = "red") @RequestParam String color,
            @Parameter(description = "Durum (true/false)", example = "true") @RequestParam boolean state) {
        String command = color.toLowerCase() + (state ? "_on" : "_off");
        mqttPublisher.publishMessage("iot/control/led", command);
        return "💡 LED komutu gönderildi: " + command;
    }

    @Operation(summary = "Servo belirli açıya döndür")
    @ApiResponse(responseCode = "200", description = "Servo komutu gönderildi",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "⚙️ Servo komutu gönderildi: servo1:90")))
    @PostMapping("/servo-control")
    public String controlServo(
            @RequestBody(description = "Servo kontrol gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ServoCommand.class),
                            examples = @ExampleObject(value = "{\"servo\":\"servo1\",\"angle\":90}")))
            @org.springframework.web.bind.annotation.RequestBody ServoCommand command) {
        String message = command.getServo() + ":" + command.getAngle();
        mqttPublisher.publishServoCommand(message);
        return "⚙️ Servo komutu gönderildi: " + message;
    }

    @Operation(summary = "Buzzer beep")
    @ApiResponse(responseCode = "200", description = "Buzzer komutu gönderildi",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "🔔 Buzzer komutu gönderildi.")))
    @PostMapping("/buzzer/beep")
    public String beepBuzzer() {
        mqttPublisher.publishMessage("iot/control/buzzer", "on");
        return "🔔 Buzzer komutu gönderildi.";
    }

    @Operation(summary = "Sıcaklık/Nem manuel güncelle")
    @ApiResponse(responseCode = "200", description = "Sensör değerleri güncellendi")
    @PostMapping("/sensor")
    public void updateSensorData(
            @Parameter(description = "Sıcaklık (°C)", example = "23.4") @RequestParam float temp,
            @Parameter(description = "Nem (%)", example = "45.2") @RequestParam float hum) {
        sensorDataService.setTemperature(temp);
        sensorDataService.setHumidity(hum);
        System.out.println("🌡 Sıcaklık: " + temp + " °C | 💧 Nem: " + hum + " %");
    }

    @Operation(summary = "RFID kart ID güncelle")
    @ApiResponse(responseCode = "200", description = "RFID değeri güncellendi")
    @PostMapping("/rfid")
    public void updateCard(
            @Parameter(description = "Kart UID", example = "A1B2C3D4") @RequestParam String cardId) {
        sensorDataService.setLastUid(cardId);
        System.out.println("📛 Yeni kart okundu ➜ " + cardId);
    }

    @Operation(summary = "Tüm durumları getir (LED/Servo/Temp/Nem/RFID)")
    @ApiResponse(responseCode = "200", description = "Durumlar başarıyla getirildi",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value =
                            "{ \"ledOn\": true, \"servo1Open\": false, \"servo2Open\": true, " +
                                    "\"temperature\": 24.1, \"humidity\": 47.0, \"lastCardID\": \"A1B2C3D4\" }")))
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("ledOn", sensorDataService.isLedOn());
        status.put("servo1Open", sensorDataService.isServo1Open());
        status.put("servo2Open", sensorDataService.isServo2Open());
        status.put("temperature", sensorDataService.getTemperature());
        status.put("humidity", sensorDataService.getHumidity());
        status.put("lastCardID", sensorDataService.getLastUid());
        return status;
    }
}
