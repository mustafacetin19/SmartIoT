package com.example.smartiot.controller;

import com.example.smartiot.model.ServoCommand;
import com.example.smartiot.service.MqttPublisher;
import com.example.smartiot.service.SensorDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.smartiot.service.DeviceService;
import com.example.smartiot.model.Device;
import java.util.List;



import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class DeviceController {

    private DeviceService deviceService;


    private final SensorDataService sensorDataService;
    private final MqttPublisher mqttPublisher;


    @Autowired
    public DeviceController(SensorDataService sensorDataService, MqttPublisher mqttPublisher) {
        this.sensorDataService = sensorDataService;
        this.mqttPublisher = mqttPublisher;
        this.deviceService =deviceService;
    }

    // Servo 1 Toggle (0° ↔ 90°)
    @PostMapping("/control/servo1")
    public void toggleServo1() {
        sensorDataService.setServo1Open(!sensorDataService.isServo1Open());
        System.out.println("Servo 1 durumu güncellendi ➜ " +
                (sensorDataService.isServo1Open() ? "90 derece" : "0 derece"));
    }

    // Servo 2 Toggle (0° ↔ 90°)
    @PostMapping("/control/servo2")
    public void toggleServo2() {
        sensorDataService.setServo2Open(!sensorDataService.isServo2Open());
        System.out.println("Servo 2 durumu güncellendi ➜ " +
                (sensorDataService.isServo2Open() ? "90 derece" : "0 derece"));
    }

    // Genel LED Toggle
    @PostMapping("/control/led")
    public void toggleLed() {
        sensorDataService.setLedOn(!sensorDataService.isLedOn());
        System.out.println("LED durumu: " + (sensorDataService.isLedOn() ? "AÇIK" : "KAPALI"));
    }

    // Renkli LED Aç/Kapat (MQTT ile)
    @PostMapping("/led/toggle")
    public String toggleLed(@RequestParam String color, @RequestParam boolean state) {
        String command = color.toLowerCase() + (state ? "_on" : "_off");
        mqttPublisher.publishMessage("iot/control/led", command);
        return "💡 LED komutu gönderildi: " + command;
    }

    // Servo belirli açıya döndür
    @PostMapping("/servo-control")
    public String controlServo(@RequestBody ServoCommand command) {
        String message = command.getServo() + ":" + command.getAngle();
        mqttPublisher.publishServoCommand(message);
        return "⚙️ Servo komutu gönderildi: " + message;
    }

    // Buzzer Beep
    @PostMapping("/buzzer/beep")
    public String beepBuzzer() {
        mqttPublisher.publishMessage("iot/control/buzzer", "on");
        return "🔔 Buzzer komutu gönderildi.";
    }

    // Sıcaklık / Nem güncelle (manuel)
    @PostMapping("/sensor")
    public void updateSensorData(@RequestParam float temp, @RequestParam float hum) {
        sensorDataService.setTemperature(temp);
        sensorDataService.setHumidity(hum);
        System.out.println("🌡 Sıcaklık: " + temp + " °C | 💧 Nem: " + hum + " %");
    }

    // RFID kart ID güncelle
    @PostMapping("/rfid")
    public void updateCard(@RequestParam String cardId) {
        sensorDataService.setLastUid(cardId);
        System.out.println("📛 Yeni kart okundu ➜ " + cardId);
    }

    @GetMapping("/devices/active")
    public List<Device> getActiveDevices() {
        return deviceService.getActiveDevices();
    }

    @PostMapping("/devices")
    public Device addDevice(@RequestBody Device device) {
        return deviceService.saveDevice(device);
    }



    // ✅ Arayüz için tüm durumları çek
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
