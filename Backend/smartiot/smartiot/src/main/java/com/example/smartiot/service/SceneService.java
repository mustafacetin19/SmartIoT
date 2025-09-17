// PATH: src/main/java/com/example/smartiot/service/SceneService.java
package com.example.smartiot.service;

import com.example.smartiot.dto.SceneCommandDto;
import com.example.smartiot.dto.SceneCreateRequest;
import com.example.smartiot.dto.SceneResponse;
import com.example.smartiot.dto.SceneUpdateRequest;
import com.example.smartiot.model.Scene;
import com.example.smartiot.model.SceneCommand;
import com.example.smartiot.repository.SceneCommandRepository;
import com.example.smartiot.repository.SceneRepository;
import com.example.smartiot.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SceneService {

    private final SceneRepository sceneRepo;
    private final SceneCommandRepository cmdRepo;
    private final UserDeviceRepository userDeviceRepo;

    @Transactional(readOnly = true)
    public List<SceneResponse> listByUser(Long userId) {
        var list = sceneRepo.findByUserIdOrderByIdAsc(userId);
        var out = new ArrayList<SceneResponse>(list.size());
        for (var s : list) out.add(toDto(s));
        return out;
    }

    @Transactional
    public SceneResponse create(SceneCreateRequest req) {
        var s = new Scene();
        s.setUserId(req.userId());
        s.setName(req.name());
        s.setEnabled(req.enabled() == null ? Boolean.TRUE : req.enabled());
        s = sceneRepo.save(s);

        if (req.commands() != null) {
            for (var c : req.commands()) {
                assertOwnedAndActive(req.userId(), c.userDeviceId());
                var sc = new SceneCommand();
                sc.setSceneId(s.getId());
                sc.setUserDeviceId(c.userDeviceId());
                sc.setCommand(c.command());
                sc.setValue(c.value());
                sc.setSortOrder(c.sortOrder() == null ? 0 : c.sortOrder());
                cmdRepo.save(sc);
            }
        }
        return toDto(s);
    }

    @Transactional
    public SceneResponse update(Long id, SceneUpdateRequest req) {
        var s = sceneRepo.findById(id).orElseThrow();
        if (req.name() != null) s.setName(req.name());
        if (req.enabled() != null) s.setEnabled(req.enabled());
        sceneRepo.save(s);

        if (req.commands() != null) {
            cmdRepo.deleteBySceneId(id);
            for (var c : req.commands()) {
                assertOwnedAndActive(s.getUserId(), c.userDeviceId());
                var sc = new SceneCommand();
                sc.setSceneId(id);
                sc.setUserDeviceId(c.userDeviceId());
                sc.setCommand(c.command());
                sc.setValue(c.value());
                sc.setSortOrder(c.sortOrder() == null ? 0 : c.sortOrder());
                cmdRepo.save(sc);
            }
        }
        return toDto(s);
    }

    @Transactional
    public void delete(Long id) {
        cmdRepo.deleteBySceneId(id);
        sceneRepo.deleteById(id);
    }

    @Transactional
    public void toggle(Long id, boolean enabled) {
        var s = sceneRepo.findById(id).orElseThrow();
        s.setEnabled(enabled);
        sceneRepo.save(s);
    }

    @Transactional(readOnly = true)
    public SceneResponse getDto(Long id) {
        return toDto(sceneRepo.findById(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public Scene get(Long id) {
        return sceneRepo.findById(id).orElseThrow();
    }

    /** Seçilen userDevice, ilgili kullanıcıya ait ve AKTİF mi? */
    private void assertOwnedAndActive(Long userId, Long userDeviceId) {
        if (userId == null || userDeviceId == null) {
            throw new IllegalArgumentException("userDeviceId ve userId zorunludur.");
        }
        var ud = userDeviceRepo.findByIdAndUser_Id(userDeviceId, userId);
        if (ud.isEmpty() || !ud.get().isActive()) {
            throw new IllegalArgumentException("Seçilen cihaz size ait değil veya aktif değil.");
        }
    }

    private SceneResponse toDto(Scene s) {
        var commands = cmdRepo.findBySceneIdOrderBySortOrderAsc(s.getId())
                .stream()
                .map(c -> new SceneCommandDto(
                        c.getUserDeviceId(), c.getCommand(), c.getValue(), c.getSortOrder()))
                .toList();
        return new SceneResponse(s.getId(), s.getUserId(), s.getName(), s.getEnabled(), commands);
    }
}
