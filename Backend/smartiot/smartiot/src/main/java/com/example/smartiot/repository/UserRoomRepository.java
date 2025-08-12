package com.example.smartiot.repository;

import com.example.smartiot.model.UserRoom;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {

    List<UserRoom> findByUser_IdAndActiveTrue(Long userId);

    Optional<UserRoom> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByUser_IdAndRoomNameIgnoreCase(Long userId, String roomName);

    @Modifying
    @Query("update UserRoom ur set ur.active=false where ur.user.id=:userId")
    int deactivateRoomsByUserId(@Param("userId") Long userId);
}
