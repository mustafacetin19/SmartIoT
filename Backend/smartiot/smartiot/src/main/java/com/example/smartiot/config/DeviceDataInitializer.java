package com.example.smartiot.config;

import com.example.smartiot.model.Device;
import com.example.smartiot.repository.DeviceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
// import org.springframework.context.annotation.Profile; // sadece dev'de çalıştırmak istersen aç
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
// @Profile("dev")
public class DeviceDataInitializer implements ApplicationRunner {

    private final DeviceRepository deviceRepository;

    public DeviceDataInitializer(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        // LED renkleri (her birine AYRI prefix veriyoruz)
        seedModel("Led-White",  "White LED",       "LEDW-",  100);
        seedModel("Led-Red",    "Red LED",         "LEDR-",  100);
        seedModel("Led-Blue",   "Blue LED",        "LEDB-",  100);
        seedModel("Led-Yellow", "Yellow LED",      "LEDY-",  100);

        // Diğer cihazlar
        seedModel("RFID",       "RFID Reader",     "RFID-",  100);
        seedModel("SERVO",      "Servo Motor",     "SERVO-", 100);
        seedModel("BUZZER",     "Buzzer",          "BUZ-",   100);
        seedModel("DHT11",      "Temp & Humidity", "DHT11-", 100);
    }

    /** Verilen model için aktif stok hedefin altındaysa eksik kadar ünite üretir. */
    private void seedModel(String deviceModel, String deviceName, String uidPrefix, int targetCount) {
        long existingActive = deviceRepository.countByDeviceModelAndActiveTrue(deviceModel);
        int need = (int) (targetCount - existingActive);

        if (need <= 0) {
            System.out.printf("✔ %s zaten hedefte/üstünde (aktif: %d, hedef: %d).%n",
                    deviceModel, existingActive, targetCount);
            return;
        }

        int start = deviceRepository.findMaxNumericSuffixForPrefix(uidPrefix) + 1;

        List<Device> batch = new ArrayList<>(need);
        for (int i = 0; i < need; i++) {
            Device d = new Device();
            d.setDeviceModel(deviceModel);
            d.setDeviceName(deviceName);
            d.setDeviceUid(uidPrefix + String.format("%06d", start + i)); // örn: LEDW-000123
            d.setActive(true);
            batch.add(d);
        }

        deviceRepository.saveAll(batch); // her çağrı kendi transaction'ında
        System.out.printf("✅ Seeded %-10s : +%d (şimdi: %d/%d)%n",
                deviceModel, need, (existingActive + need), targetCount);
    }
}
