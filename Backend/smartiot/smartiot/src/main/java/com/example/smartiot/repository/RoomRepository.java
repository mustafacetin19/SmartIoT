package com.example.smartiot.repository;

import com.example.smartiot.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByName(String name);
}
