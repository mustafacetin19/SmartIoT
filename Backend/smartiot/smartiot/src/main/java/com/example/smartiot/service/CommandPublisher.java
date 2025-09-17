package com.example.smartiot.service;

public interface CommandPublisher {
    void publishUserDevice(Long userDeviceId, String command, String value);
}
