package com.example.smartiot.repository;

import com.example.smartiot.model.UserDevice;
import com.example.smartiot.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    List<UserDevice> findByUser(User user);
    List<UserDevice> findByUserAndActiveTrue(User user);
    List<UserDevice> findByUserId(Long userId);

    // Kullanıcının oda atanmamış (havuz) ve aktif cihazları
    List<UserDevice> findByUser_IdAndUserRoomIsNullAndActiveTrue(Long userId);

    // Havuzda aynı cihazdan var mı?
    boolean existsByUser_IdAndDevice_IdAndUserRoomIsNull(Long userId, Long deviceId);

    Optional<UserDevice> findByIdAndUser_Id(Long id, Long userId);

    // ⬇️ EKLENDİ: “ilk atama” kararını verebilmek için sayım
    long countByUser_IdAndDevice_IdAndUserRoomIsNullAndActiveTrue(Long userId, Long deviceId);

    @Modifying
    @Query("update UserDevice ud set ud.active=false where ud.user.id=:userId")
    int deactivateUserDevicesByUserId(@Param("userId") Long userId);
}
