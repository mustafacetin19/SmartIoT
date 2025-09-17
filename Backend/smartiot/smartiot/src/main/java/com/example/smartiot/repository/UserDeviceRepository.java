// PATH: src/main/java/com/example/smartiot/repository/UserDeviceRepository.java
package com.example.smartiot.repository;

import com.example.smartiot.model.Device;
import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    // ---- Basit sorgular ----
    List<UserDevice> findByUser(User user);
    List<UserDevice> findByUserAndActiveTrue(User user);

    // userId alanına ilişki üzerinden iniyoruz
    List<UserDevice> findByUser_Id(Long userId);

    // Kullanıcının oda atanmamış (havuz) ve aktif cihazları
    List<UserDevice> findByUser_IdAndUserRoomIsNullAndActiveTrue(Long userId);

    // Havuzda aynı cihazdan var mı?
    boolean existsByUser_IdAndDevice_IdAndUserRoomIsNull(Long userId, Long deviceId);

    // Kayıt doğrulama
    Optional<UserDevice> findByIdAndUser_Id(Long id, Long userId);

    // Havuz sayısı
    long countByUser_IdAndDevice_IdAndUserRoomIsNullAndActiveTrue(Long userId, Long deviceId);

    // Yetki kontrolü (aktif)
    boolean existsByUser_IdAndDevice_IdAndActiveTrue(Long userId, Long deviceId);

    // ✅ Yalnızca bu kullanıcıya ait + aktif (active=true) + status=ACTIVE (veya null) user_device kayıtları
    @Query("""
      select ud from UserDevice ud
      where ud.user.id = :userId
        and ud.active = true
        and (ud.status = com.example.smartiot.model.UserDevice.Status.ACTIVE or ud.status is null)
      order by ud.id asc
    """)
    List<UserDevice> findAllActiveForUser(@Param("userId") Long userId);

    // Kullanıcının kullanabildiği aktif Device listesi
    @Query("""
      select d from Device d
      where d.active = true and d.id in (
        select ud.device.id from UserDevice ud
        where ud.user.id = :userId and ud.active = true
      )
    """)
    List<Device> findAllAllowedDevicesForUser(@Param("userId") Long userId);

    // Bir kullanıcıya ait tüm UserDevice kayıtlarını pasif yap
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("update UserDevice ud set ud.active = false where ud.user.id = :userId")
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
