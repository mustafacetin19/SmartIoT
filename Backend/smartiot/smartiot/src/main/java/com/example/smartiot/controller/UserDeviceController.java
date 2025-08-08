package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.service.UserDeviceService;
import com.example.smartiot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-devices")
@CrossOrigin(origins = "http://localhost:3000")
public class UserDeviceController {

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private UserService userService;

    // Belirli kullanıcıya ait cihazları getir (ID üzerinden)
    @GetMapping("/user/{userId}")
    public List<UserDevice> getUserDevices(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return userDeviceService.getDevicesByUser(user);
    }

    // Yeni cihaz-kullanıcı eşlemesi oluştur
    @PostMapping
    public UserDevice addUserDevice(@RequestBody UserDevice userDevice) {
        return userDeviceService.save(userDevice);
    }

    // Aktiflik güncelle
    @PutMapping("/{id}/status")
    public UserDevice updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return userDeviceService.setActiveStatus(id, active);
    }

    // ❌ Remove yerine kullanılacak: sadece active = false yapar
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUserDevice(@PathVariable Long id) {
        boolean success = userDeviceService.deactivateUserDevice(id);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Gerçek silme işlemi (kullanılmıyor artık)
    @DeleteMapping("/{id}")
    public void deleteUserDevice(@PathVariable Long id) {
        userDeviceService.deleteById(id);
    }

    // Tüm user-device kayıtlarını getir
    @GetMapping
    public List<UserDevice> getAllUserDevices() {
        return userDeviceService.getAllUserDevices();
    }
}
