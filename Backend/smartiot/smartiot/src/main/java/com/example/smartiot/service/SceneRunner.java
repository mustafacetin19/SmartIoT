package com.example.smartiot.service;

import com.example.smartiot.repository.SceneCommandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SceneRunner {
    private final SceneCommandRepository cmdRepo;
    private final CommandPublisher publisher;

    @Transactional(readOnly = true)
    public void runScene(Long sceneId) {
        var commands = cmdRepo.findBySceneIdOrderBySortOrderAsc(sceneId);
        for (var c : commands) {
            publisher.publishUserDevice(c.getUserDeviceId(), c.getCommand(), c.getValue());
        }
    }
}
