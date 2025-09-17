package com.example.smartiot.repository;

import com.example.smartiot.model.SceneCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SceneCommandRepository extends JpaRepository<SceneCommand, Long> {
    List<SceneCommand> findBySceneIdOrderBySortOrderAsc(Long sceneId);
    void deleteBySceneId(Long sceneId);
}
