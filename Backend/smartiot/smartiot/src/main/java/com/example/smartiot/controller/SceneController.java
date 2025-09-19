// PATH: src/main/java/com/example/smartiot/controller/SceneController.java
package com.example.smartiot.controller;

import com.example.smartiot.dto.SceneCreateRequest;
import com.example.smartiot.dto.SceneResponse;
import com.example.smartiot.dto.SceneUpdateRequest;
import com.example.smartiot.service.SceneRunner;
import com.example.smartiot.service.SceneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SceneController {

    private final SceneService service;
    private final SceneRunner runner;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return ResponseEntity.badRequest().body("Missing required request parameter: userId");
        }
        List<SceneResponse> res = service.listByUser(userId);
        return ResponseEntity.ok(res);
    }

    @PostMapping
    public SceneResponse create(@RequestBody SceneCreateRequest req) {
        var res = service.create(req);

        return res;
    }

    @PutMapping("/{id}")
    public SceneResponse update(@PathVariable Long id, @RequestBody SceneUpdateRequest req) {
        var res = service.update(id, req);

        return res;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);

    }

    @PostMapping("/{id}/run")
    public void runNow(@PathVariable Long id) {
        runner.runScene(id);
    }

    @PostMapping("/{id}/toggle")
    public void toggle(@PathVariable Long id, @RequestParam boolean enabled) {
        service.toggle(id, enabled);

    }
}
