package com.example.smartiot.service;

import com.example.smartiot.model.User;
import com.example.smartiot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoomRepository userRoomRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final DeviceRepository deviceRepository;

    public User register(User user) {
        user.setActive(true);
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.filter(u -> u.isActive() && u.getPassword().equals(password));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        // 1) kullanıcı pasif
        userRepository.deactivateById(userId);

        // 2) kullanıcı odaları pasif
        userRoomRepository.deactivateRoomsByUserId(userId);

        // 3) user_devices pasif
        userDeviceRepository.deactivateUserDevicesByUserId(userId);

        // 4) cihazlar: başka aktif kullanıcı yoksa pasif (güvenli mod)
        deviceRepository.deactivateDevicesOfUserIfNotUsedByOthers(userId);
    }
}
