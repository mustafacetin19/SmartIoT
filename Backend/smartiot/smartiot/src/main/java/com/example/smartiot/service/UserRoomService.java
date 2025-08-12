package com.example.smartiot.service;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserRoom;
import com.example.smartiot.repository.UserRoomRepository;
import com.example.smartiot.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoomService {

    private final UserRoomRepository repo;
    private final UserService userService;

    public UserRoomService(UserRoomRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    public List<UserRoom> listActiveRooms(Long userId) {
        return repo.findByUser_IdAndActiveTrue(userId);
    }

    @Transactional
    public UserRoom create(Long userId, String roomName) {
        if (repo.existsByUser_IdAndRoomNameIgnoreCase(userId, roomName)) {
            throw new IllegalArgumentException("Bu oda adı zaten mevcut.");
        }
        User owner = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
        UserRoom r = UserRoom.builder()
                .user(owner)
                .roomName(roomName)
                .active(true)
                .build();
        return repo.save(r);
    }

    @Transactional
    public void deactivate(Long userId, Long roomId) {
        UserRoom r = repo.findByIdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Oda bulunamadı"));
        r.setActive(false);
        repo.save(r);
    }
}
