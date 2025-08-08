package com.example.smartiot.service;

import org.springframework.stereotype.Service;

@Service
public class SensorDataService {

    private volatile float temperature = 0.0f;
    private volatile float humidity = 0.0f;
    private volatile String lastUid = "Yok";
    private volatile boolean ledOn = false;
    private volatile boolean servo1Open = false;
    private volatile boolean servo2Open = false;

    // Sıcaklık
    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    // Nem
    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    // RFID
    public String getLastUid() {
        return lastUid;
    }

    public void setLastUid(String lastUid) {
        this.lastUid = lastUid;
    }

    // LED
    public boolean isLedOn() {
        return ledOn;
    }

    public void setLedOn(boolean ledOn) {
        this.ledOn = ledOn;
    }

    // Servo 1
    public boolean isServo1Open() {
        return servo1Open;
    }

    public void setServo1Open(boolean servo1Open) {
        this.servo1Open = servo1Open;
    }

    // Servo 2
    public boolean isServo2Open() {
        return servo2Open;
    }

    public void setServo2Open(boolean servo2Open) {
        this.servo2Open = servo2Open;
    }
}