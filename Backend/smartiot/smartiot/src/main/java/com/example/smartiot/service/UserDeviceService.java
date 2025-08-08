package com.example.smartiot.service;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.repository.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserDeviceService {

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    public List<UserDevice> getDevicesByUser(User user) {
        return userDeviceRepository.findByUser(user);
    }

    public List<UserDevice> getActiveDevicesByUser(User user) {
        return userDeviceRepository.findByUserAndActiveTrue(user);
    }

    public UserDevice save(UserDevice userDevice) {
        return userDeviceRepository.save(userDevice);
    }

    public Optional<UserDevice> getById(Long id) {
        return userDeviceRepository.findById(id);
    }

    public void deleteById(Long id) {
        userDeviceRepository.deleteById(id);
    }

    public List<UserDevice> getAllUserDevices() {
        return userDeviceRepository.findAll();
    }

    public UserDevice setActiveStatus(Long id, boolean active) {
        UserDevice userDevice = userDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserDevice not found"));
        userDevice.setActive(active);
        return userDeviceRepository.save(userDevice);
    }

    // 🔥 Yeni method: sadece active = false yapar
    public boolean deactivateUserDevice(Long id) {
        Optional<UserDevice> optionalDevice = userDeviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            UserDevice device = optionalDevice.get();
            device.setActive(false);
            userDeviceRepository.save(device);
            return true;
        } else {
            return false;
        }
    }
}
