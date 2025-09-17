package com.example.smartiot.service.impl;

import com.example.smartiot.model.UserDevice;
import com.example.smartiot.repository.UserDeviceRepository;
import com.example.smartiot.service.CommandPublisher;
import com.example.smartiot.service.MqttPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("mqtt") // <- mqtt profili açıkken bu çalışır
@RequiredArgsConstructor
public class MqttCommandPublisher implements CommandPublisher {

    private final UserDeviceRepository userDeviceRepo;
    private final MqttPublisher mqttPublisher;

    @Override
    public void publishUserDevice(Long userDeviceId, String command, String value) {
        if (userDeviceId == null) {
            log.warn("[SCENE][MQTT] userDeviceId null geldi, komut yayınlanmadı.");
            return;
        }

        UserDevice ud = userDeviceRepo.findById(userDeviceId).orElse(null);
        if (ud == null || ud.getDevice() == null) {
            log.warn("[SCENE][MQTT] userDevice bulunamadı: {}", userDeviceId);
            return;
        }
        if (!ud.isActive() || ud.getStatus() != null && ud.getStatus() != UserDevice.Status.ACTIVE) {
            log.warn("[SCENE][MQTT] userDevice pasif: {}", userDeviceId);
            return;
        }

        Long deviceId = ud.getDevice().getId();
        String model   = (ud.getDevice().getDeviceModel() != null) ? ud.getDevice().getDeviceModel().toUpperCase() : "";

        try {
            // Basit sözleşme:
            // command: ON / OFF / SET
            // value  : LED için boş/önemsiz, SERVO için açı (0-180), BUZZER için beep|beep2|beep3
            if (model.startsWith("LED")) {
                boolean on = "ON".equalsIgnoreCase(command);
                mqttPublisher.publishLedByDeviceId(deviceId, on);

            } else if (model.startsWith("SERVO")) {
                int angle = 0;
                if ("SET".equalsIgnoreCase(command) && value != null && !value.isBlank()) {
                    try { angle = Math.max(0, Math.min(180, Integer.parseInt(value.trim()))); }
                    catch (Exception ignore) {}
                } else if ("ON".equalsIgnoreCase(command)) {
                    angle = 90; // varsayılan
                } else {
                    angle = 0; // OFF
                }
                mqttPublisher.publishServoByDeviceId(deviceId, angle);

            } else if (model.startsWith("BUZZER")) {
                String action = (value != null && !value.isBlank())
                        ? value.trim()
                        : ("ON".equalsIgnoreCase(command) ? "beep" : "off");
                mqttPublisher.publishBuzzerByDeviceId(deviceId, action);

            } else {
                log.info("[SCENE][MQTT] Model için özel yayıncı yok: {} (deviceId={})", model, deviceId);
            }
        } catch (Exception e) {
            log.error("[SCENE][MQTT] Yayın hatası udId={} model={} cmd={} val={}",
                    userDeviceId, model, command, value, e);
        }
    }
}
