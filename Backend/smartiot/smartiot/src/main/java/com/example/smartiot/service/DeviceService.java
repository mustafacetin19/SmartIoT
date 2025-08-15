package com.example.smartiot.service;

import com.example.smartiot.model.Device;
import com.example.smartiot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public List<Device> getAllDevices() { // eski uyumluluk için
        return deviceRepository.findAll();
    }

    public List<Device> findActive() {
        return deviceRepository.findByActiveTrue();
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    // Kataloğu create/delete kapattık ama methodlar kalsın (admin/seed olabilir)
    public Device saveDevice(Device device) { return deviceRepository.save(device); }
    public void deleteDevice(Long id) { deviceRepository.deleteById(id); }

    public Device updateDeviceStatus(Long id, boolean active) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cihaz bulunamadı"));
        device.setActive(active);
        return deviceRepository.save(device);
    }
}
