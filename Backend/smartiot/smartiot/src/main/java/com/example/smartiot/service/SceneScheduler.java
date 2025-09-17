package com.example.smartiot.service;

import com.example.smartiot.repository.SceneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cron kaldırıldığı için bu sınıf şu an "no-op" scheduler gibi davranır.
 * Enabled durumlarını hafızada tutar; zamanlayıcı kurulumu yapmaz.
 */
@Service
@RequiredArgsConstructor
public class SceneScheduler implements SmartLifecycle {

    private final SceneRepository sceneRepo;
    @SuppressWarnings("unused")
    private final SceneRunner runner;

    // Sadece durum takibi için basit bir harita
    private final Map<Long, Boolean> enabledMap = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    @Override
    public void start() {
        reloadAll();
        running = true;
    }

    @Override
    public void stop() {
        enabledMap.clear();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public void reloadAll() {
        enabledMap.clear();
        sceneRepo.findAll().forEach(s ->
                enabledMap.put(s.getId(), Boolean.TRUE.equals(s.getEnabled()))
        );
    }

    /** Cron olmadığı için yalnızca "enabled" durumunu güncelliyoruz. */
    public void rescheduleOne(Long sceneId, boolean enabled) {
        if (enabled) {
            enabledMap.put(sceneId, true);
        } else {
            enabledMap.remove(sceneId);
        }
    }
}
