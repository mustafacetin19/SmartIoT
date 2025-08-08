package com.example.smartiot.repository;

import com.example.smartiot.model.UserDevice;
import com.example.smartiot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    List<UserDevice> findByUser(User user);

    List<UserDevice> findByUserAndActiveTrue(User user);

    List<UserDevice> findByUserId(Long userId);
}
