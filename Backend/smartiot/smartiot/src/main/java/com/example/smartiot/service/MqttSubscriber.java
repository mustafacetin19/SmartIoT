package com.example.smartiot.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class MqttSubscriber implements MqttCallback {

    private final TelemetryService telemetryService;

    private final String brokerUrl = "tcp://192.168.199.225:1883";
    private final String clientId  = "SpringBootSubscriber";
    private MqttClient client;

    // Jackson (Spring Boot zaten getirir)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void start() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.setCallback(this);
            client.connect(options);

            // ESP32 publish:
            //  sensor/{deviceId}/status  -> {"temperature":..,"humidity":..}
            //  rfid/{deviceId}/last      -> CARD_UID
            client.subscribe("sensor/+/status", 1);
            client.subscribe("rfid/+/last", 1);

            System.out.println("✅ MQTT Subscriber hazır.");
        } catch (Exception e) {
            System.err.println("❌ MQTT Subscriber başlatılamadı.");
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() {
        try {
            if (client != null && client.isConnected()) client.disconnect();
        } catch (Exception ignored) {}
    }

    @Override public void connectionLost(Throwable cause) {
        System.err.println("⚠️ MQTT bağlantısı koptu: " + (cause != null ? cause.getMessage() : ""));
    }
    @Override public void deliveryComplete(IMqttDeliveryToken token) {}

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        try {
            // topic: <kind>/<deviceId>/<suffix>
            String[] parts = topic.split("/");
            if (parts.length < 3) return;

            String kind = parts[0];       // "sensor" | "rfid"
            Long deviceId = Long.parseLong(parts[1]);
            String suffix = parts[2];     // "status" | "last"

            if ("sensor".equals(kind) && "status".equals(suffix)) {
                // JSON: {"temperature":23.1,"humidity":46.5}
                JsonNode root = objectMapper.readTree(payload);
                Double t = root.hasNonNull("temperature") ? root.get("temperature").asDouble() : null;
                Double h = root.hasNonNull("humidity")    ? root.get("humidity").asDouble()    : null;
                telemetryService.updateSensor(deviceId, t, h);
            } else if ("rfid".equals(kind) && "last".equals(suffix)) {
                telemetryService.updateRfid(deviceId, payload.trim());
            }
        } catch (Exception e) {
            System.err.println("❌ MQTT mesaj parse hatası. topic=" + topic + " payload=" + payload);
            e.printStackTrace();
        }
    }
}
