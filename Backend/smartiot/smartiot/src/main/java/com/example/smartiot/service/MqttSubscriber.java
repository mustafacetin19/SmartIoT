package com.example.smartiot.service;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriber {

    // SADECE URL GÜNCELLENDİ
    private static final String BROKER_URL = "tcp://SENIN_BROKER_IPN:1883";
    private static final String CLIENT_ID = "SpringBootSubscriber";

    private MqttClient client;

    private final SensorDataService sensorDataService;

    @Autowired
    public MqttSubscriber(SensorDataService sensorDataService) {
        this.sensorDataService = sensorDataService;
    }

    @PostConstruct
    public void subscribe() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID, null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(5);

            client.connect(options);
            System.out.println("✅ MQTT bağlantısı başarılı, dinleme başlatılıyor...");

            // ✅ Gerekli topic'lere abone ol
            subscribeTopic("rfid/uid", this::handleRfid);
            subscribeTopic("dht/data", this::handleDhtData);
            subscribeTopic("led/status", this::handleLedStatus);
            subscribeTopic("buzzer/status", this::handleBuzzerStatus);
            subscribeTopic("servo/status", this::handleServoStatus);

            System.out.println("🔁 MQTT dinleyiciler aktif durumda.");

        } catch (MqttException e) {
            System.err.println("❌ MQTT dinleyici başlatılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void subscribeTopic(String topic, IMqttMessageListener listener) throws MqttException {
        client.subscribe(topic, listener);
        System.out.println("🔔 Dinleniyor ➜ " + topic);
    }

    // === Handler: RFID ===
    private void handleRfid(String topic, MqttMessage message) {
        String uid = new String(message.getPayload()).trim();
        if (!uid.isEmpty()) {
            sensorDataService.setLastUid(uid);
            System.out.println("📛 [RFID] Yeni kart ID ➜ " + uid);
        } else {
            System.out.println("⚠ Boş RFID verisi geldi.");
        }
    }

    // === Handler: DHT11 ===
    private void handleDhtData(String topic, MqttMessage message) {
        String payload = new String(message.getPayload()).trim();
        System.out.println("📥 [DHT11] Ham veri ➜ " + payload);

        try {
            String[] parts = payload.split(";");
            if (parts.length == 2) {
                float temp = Float.parseFloat(parts[0].split("=")[1]);
                float hum = Float.parseFloat(parts[1].split("=")[1]);

                sensorDataService.setTemperature(temp);
                sensorDataService.setHumidity(hum);

                System.out.println("🌡 Sıcaklık: " + temp + " °C");
                System.out.println("💧 Nem: " + hum + " %");
            } else {
                System.err.println("⚠ Hatalı DHT formatı. Beklenen: temperature=..;humidity=..");
            }
        } catch (Exception e) {
            System.err.println("❗ DHT veri ayrıştırma hatası: " + e.getMessage());
        }
    }

    // === Handler: LED Durumu (opsiyonel) ===
    private void handleLedStatus(String topic, MqttMessage message) {
        String status = new String(message.getPayload()).trim();
        System.out.println("💡 [LED] Durum ➜ " + status);
        // İstersen burada sensorDataService.setLedOn(...) çağırabilirsin
    }

    // === Handler: Buzzer Durumu (opsiyonel) ===
    private void handleBuzzerStatus(String topic, MqttMessage message) {
        String status = new String(message.getPayload()).trim();
        System.out.println("🔔 [Buzzer] Durum ➜ " + status);
    }

    // === Handler: Servo Durumu (opsiyonel) ===
    private void handleServoStatus(String topic, MqttMessage message) {
        String status = new String(message.getPayload()).trim();
        System.out.println("⚙ [Servo] Durum ➜ " + status);
    }
}
