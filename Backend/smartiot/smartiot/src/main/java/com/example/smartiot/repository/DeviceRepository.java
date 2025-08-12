package com.example.smartiot.repository;

import com.example.smartiot.model.Device;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceUid(String deviceUid);
    List<Device> findByActiveTrue();
    boolean existsByDeviceUid(String deviceUid);

    @Modifying
    @Query(value = """
        update devices d
        set active = false
        where d.id in (
            select device_id from user_devices where user_id = :userId
        )
        and not exists (
            select 1
            from user_devices ud2
            join users u2 on u2.id = ud2.user_id
            where ud2.device_id = d.id
              and ud2.user_id <> :userId
              and ud2.active = true
              and u2.active = true
        )
        """, nativeQuery = true)
    int deactivateDevicesOfUserIfNotUsedByOthers(@Param("userId") Long userId);
}
