package com.example.smartiot.service;

import com.example.smartiot.model.Device;
import com.example.smartiot.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device saveDevice(Device device) {
        return deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public List<Device> getActiveDevices() {
        return deviceRepository.findByActiveTrue();
    }

    public Device updateDeviceStatus(Long id, boolean active) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı"));
        device.setActive(active);
        return deviceRepository.save(device);
    }
}
