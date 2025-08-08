package com.example.smartiot.controller;

import com.example.smartiot.model.Device;
import com.example.smartiot.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:3000")
public class DeviceManagementController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        return deviceService.saveDevice(device);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }

    @PutMapping("/{id}/status")
    public Device updateDeviceStatus(@PathVariable Long id, @RequestParam boolean active) {
        return deviceService.updateDeviceStatus(id, active);
    }
}
