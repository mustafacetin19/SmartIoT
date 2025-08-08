package com.example.smartiot.config;

import com.example.smartiot.model.Room;
import com.example.smartiot.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomDataInitializer {

    @Autowired
    private RoomRepository roomRepository;

    @PostConstruct
    public void init() {
        List<String> defaultRooms = List.of(
                "Salon", "Mutfak", "Yatak Odası 1", "Yatak Odası 2",
                "Banyo", "Çocuk Odası", "Misafir Odası", "Ofis", "Koridor"
        );

        for (String roomName : defaultRooms) {
            if (!roomRepository.existsByName(roomName)) {
                Room room = new Room();
                room.setName(roomName);
                roomRepository.save(room);
            }
        }
    }
}
