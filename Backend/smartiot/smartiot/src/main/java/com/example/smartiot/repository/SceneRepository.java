package com.example.smartiot.repository;

import com.example.smartiot.model.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByUserIdOrderByIdAsc(Long userId);
}
