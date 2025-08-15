package com.example.smartiot.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelemetryService {

    @Data @AllArgsConstructor
    public static class SensorReading {
        private Double temperature;
        private Double humidity;
    }

    private final ConcurrentHashMap<Long, SensorReading> sensors = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, String> rfids = new ConcurrentHashMap<>();

    public void updateSensor(Long deviceId, Double temperature, Double humidity) {
        sensors.put(deviceId, new SensorReading(temperature, humidity));
    }

    public SensorReading getSensor(Long deviceId) {
        return sensors.get(deviceId);
    }

    public void updateRfid(Long deviceId, String cardId) {
        rfids.put(deviceId, cardId);
    }

    public String getRfid(Long deviceId) {
        return rfids.get(deviceId);
    }
}
