package com.example.smartiot.repository;

import java.util.List;
import com.example.smartiot.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByDeviceUid(String deviceUid);

    List<Device> findByActiveTrue();

    boolean existsByDeviceUid(String deviceUid);

}
