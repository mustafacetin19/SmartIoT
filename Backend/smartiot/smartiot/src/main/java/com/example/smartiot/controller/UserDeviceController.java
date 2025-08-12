package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.service.UserDeviceService;
import com.example.smartiot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-devices")
@CrossOrigin(origins = "http://localhost:3000")
public class UserDeviceController {

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private UserService userService;

    @GetMapping("/user/{userId}")
    public List<UserDevice> getUserDevices(@PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return userDeviceService.getDevicesByUser(user);
    }

    @PutMapping("/{id}/status")
    public UserDevice updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return userDeviceService.setActiveStatus(id, active);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUserDevice(@PathVariable Long id) {
        boolean success = userDeviceService.deactivateUserDevice(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public void deleteUserDevice(@PathVariable Long id) {
        userDeviceService.deleteById(id);
    }

    @GetMapping
    public List<UserDevice> getAllUserDevices() {
        return userDeviceService.getAllUserDevices();
    }

    // ===================== ESKİ HAVUZ AKIŞI =====================
    @PostMapping("/register")
    public UserDevice registerOwned(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String deviceModel = body.get("deviceModel").toString();
        String deviceName  = body.get("deviceName").toString();
        String deviceUid   = body.get("deviceUid").toString();
        String alias       = body.getOrDefault("alias","").toString();
        return userDeviceService.registerOwnedDevice(userId, deviceModel, deviceName, deviceUid, alias);
    }

    @GetMapping("/pool/{userId}")
    public List<UserDevice> pool(@PathVariable Long userId) {
        return userDeviceService.getPool(userId);
    }

    // Havuzdan odaya: havuz item'ı korunur, kopyası odaya eklenir
    @PutMapping("/{userDeviceId}/assign")
    public UserDevice assign(@PathVariable Long userDeviceId, @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long userRoomId = Long.valueOf(body.get("userRoomId").toString());
        String assignedName = body.get("assignedName").toString();
        return userDeviceService.assignFromPool(userDeviceId, userId, userRoomId, assignedName);
    }

    // ===================== YENİ: KATALOGTAN DOĞRUDAN EKLE =====================
    // body: { userId, deviceId, userRoomId, assignedName }
    @PostMapping("/add")
    public UserDevice addFromCatalog(@RequestBody Map<String, Object> body) {
        Long userId     = Long.valueOf(body.get("userId").toString());
        Long deviceId   = Long.valueOf(body.get("deviceId").toString());    // devices.id
        Long userRoomId = Long.valueOf(body.get("userRoomId").toString());  // user_room.id
        String assigned = body.getOrDefault("assignedName", "").toString();
        return userDeviceService.addDeviceToRoom(userId, deviceId, userRoomId, assigned);
    }
}
