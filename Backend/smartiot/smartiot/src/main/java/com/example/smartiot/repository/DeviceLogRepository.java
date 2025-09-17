package com.example.smartiot.repository;

import com.example.smartiot.model.DeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceLogRepository extends JpaRepository<DeviceLog, Long> {
    List<DeviceLog> findTop100ByOrderByTsDesc();
}
