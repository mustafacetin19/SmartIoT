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

    // Havuz sayısı
    long countByUser_IdAndDevice_IdAndUserRoomIsNullAndActiveTrue(Long userId, Long deviceId);

    // Yetki
    boolean existsByUserIdAndDeviceIdAndActiveTrue(Long userId, Long deviceId);

    // Kullanıcının kullanabildiği üniteler
    @Query("""
      select d from Device d
      where d.active = true and d.id in (
        select ud.device.id from UserDevice ud
        where ud.user.id = :userId and ud.active = true
      )
    """)
    List<com.example.smartiot.model.Device> findAllAllowedDevicesForUser(@Param("userId") Long userId);

    @Modifying
    @Query("update UserDevice ud set ud.active=false where ud.user.id=:userId")
    int deactivateUserDevicesByUserId(@Param("userId") Long userId);

    // ✅ Aynı kullanıcı + aynı model için REZERVE kayıt (INACTIVE_RESERVED) → reaktivasyon
    @Query("""
      select ud from UserDevice ud
      where ud.user.id = :userId
        and ud.status = com.example.smartiot.model.UserDevice.Status.INACTIVE_RESERVED
        and ud.device.deviceModel = :model
      order by ud.id asc
    """)
    Optional<UserDevice> findFirstReservedByUserAndModel(@Param("userId") Long userId,
                                                         @Param("model") String model);
}
