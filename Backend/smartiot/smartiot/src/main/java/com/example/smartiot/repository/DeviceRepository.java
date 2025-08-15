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

    // ✅ EK: aktif stok sayısı (initializer bunu kullanıyor)
    long countByDeviceModelAndActiveTrue(String deviceModel);

    // Kullanıcının tüm cihazlarını pasifleştirme işleminde kullanılan native güncelleme (sende vardı)
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

    // ✅ DÜZELTİLDİ: "boş ünite" seçiminde artık aktiflik şartı yok.
    // user_devices'ta herhangi bir kayıt (aktif/pasif) varsa o ünite DOLU kabul edilir.
    @Query(value = """
      SELECT d.id
      FROM devices d
      LEFT JOIN user_devices ud
        ON ud.device_id = d.id
      WHERE d.active = true
        AND d.device_model = :model
        AND ud.id IS NULL
      ORDER BY d.id
      LIMIT 1
    """, nativeQuery = true)
    Long pickOneAvailableDeviceIdByModel(@Param("model") String model);

    // ✅ YENİ: model bazında stok sayımı
    @Query(value = """
      SELECT d.device_model AS model,
             COUNT(*) AS total,
             SUM(CASE WHEN ud.id IS NULL THEN 1 ELSE 0 END) AS available
      FROM devices d
      LEFT JOIN user_devices ud
        ON ud.device_id = d.id
      WHERE d.active = true
      GROUP BY d.device_model
    """, nativeQuery = true)
    List<Object[]> stockByModel();

    // ✅ EK: UID prefix'i için en büyük sayısal son ek (initializer bunu kullanıyor)
    @Query(value = """
      SELECT COALESCE(MAX(CAST(SUBSTRING(device_uid FROM '\\\\d+$') AS INTEGER)), 0)
      FROM devices
      WHERE device_uid LIKE :prefix || '%'
    """, nativeQuery = true)
    int findMaxNumericSuffixForPrefix(@Param("prefix") String prefix);
}
