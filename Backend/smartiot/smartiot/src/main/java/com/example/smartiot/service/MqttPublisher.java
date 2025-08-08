package com.example.smartiot.service;

import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class MqttPublisher {

    private final String brokerUrl = "tcp://192.168.63.225:1883";
    private final String clientId = "SpringBootPublisher";
    private MqttClient client;

    public MqttPublisher() {
        try {
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        } catch (MqttException e) {
            System.err.println("❌ MQTT istemcisi oluşturulamadı.");
            e.printStackTrace();
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initConnection() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("✅ MQTT bağlantısı başarıyla kuruldu.");
        } catch (MqttException e) {
            System.err.println("❌ MQTT bağlantısı başarısız.");
            e.printStackTrace();
        }
    }

    public void publishMessage(String topic, String message) {
        try {
            if (client == null || !client.isConnected()) {
                System.out.println("🔄 MQTT bağlı değil, yeniden bağlanılıyor...");
                initConnection();
            }

            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(1);
            client.publish(topic, mqttMessage);
            System.out.println("📤 MQTT mesajı gönderildi ➜ Topic: " + topic + " | Mesaj: " + message);
        } catch (MqttException e) {
            System.err.println("❌ MQTT mesajı gönderilemedi.");
            e.printStackTrace();
        }
    }

    public void publishServoCommand(String message) {
        publishMessage("servo/control", message);
    }

    public void publishLedCommand(String message) {
        publishMessage("iot/control/led", message);
    }

    public void publishBuzzerCommand(String message) {
        publishMessage("iot/control/buzzer", message);
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
                System.out.println("🔌 MQTT bağlantısı kapatıldı.");
            }
        } catch (MqttException e) {
            System.err.println("❌ MQTT bağlantısı kapatılamadı.");
            e.printStackTrace();
        }
    }
}