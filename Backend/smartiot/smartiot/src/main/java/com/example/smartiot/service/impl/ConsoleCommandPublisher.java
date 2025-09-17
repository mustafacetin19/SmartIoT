package com.example.smartiot.service.impl;

import com.example.smartiot.service.CommandPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"!mqtt"})
public class ConsoleCommandPublisher implements CommandPublisher {
    @Override
    public void publishUserDevice(Long userDeviceId, String command, String value) {
        log.info("[SCENE] userDeviceId={} command={} value={}", userDeviceId, command, value);
    }
}
