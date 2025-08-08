package com.example.smartiot.config;

import com.example.smartiot.model.Device;
import com.example.smartiot.repository.DeviceRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceDataInitializer {

    @Autowired
    private DeviceRepository deviceRepository;

    @PostConstruct
    public void init() {
        createIfNotExist("RFID-001", "RFID Reader", "RFID");
        createIfNotExist("LED-001", "White LED", "LED-White");
        createIfNotExist("LED-002", "Red LED", "LED-Red");
        createIfNotExist("LED-003", "Blue LED", "LED-Blue");
        createIfNotExist("LED-004", "Yellow LED", "LED-Yellow");
        createIfNotExist("BUZ-001", "Buzzer", "BUZZER");
        createIfNotExist("SERVO-001", "Servo Motor 1", "SERVO-1");
        createIfNotExist("SERVO-002", "Servo Motor 2", "SERVO-2");
        createIfNotExist("DHT11-001", "Temp & Humidity", "DHT11");

        System.out.println("Varsayılan cihaz kontrolü tamamlandı.");
    }

    private void createIfNotExist(String uid, String name, String model) {
        if (!deviceRepository.existsByDeviceUid(uid)) {
            deviceRepository.save(new Device(uid, name, model, true));
            System.out.println("✅ Eklendi: " + name);
        } else {
            System.out.println("ℹ️ Zaten mevcut: " + uid);
        }
    }
}
