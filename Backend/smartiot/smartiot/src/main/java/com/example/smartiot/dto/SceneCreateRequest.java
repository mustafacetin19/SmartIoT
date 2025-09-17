package com.example.smartiot.dto;

import java.util.List;

public record SceneCreateRequest(
        Long userId,
        String name,
        Boolean enabled,
        List<SceneCommandDto> commands
) {}
