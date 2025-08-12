package com.example.smartiot.controller;

import com.example.smartiot.model.UserRoom;
import com.example.smartiot.service.UserRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-rooms")
@CrossOrigin(origins = "http://localhost:3000")
public class UserRoomController {

    private final UserRoomService service;

    public UserRoomController(UserRoomService service) {
        this.service = service;
    }

    // Kullanıcının aktif odaları
    @GetMapping("/by-user/{userId}")
    public List<UserRoom> list(@PathVariable Long userId) {
        return service.listActiveRooms(userId);
    }

    // Oda oluştur
    @PostMapping
    public UserRoom create(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String roomName = body.get("roomName").toString();
        return service.create(userId, roomName);
    }

    // Odayı pasifleştir
    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id, @RequestParam Long userId) {
        service.deactivate(userId, id);
    }
}
