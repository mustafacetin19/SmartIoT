package com.example.smartiot.dto;

import java.util.List;

public record SceneResponse(
        Long id,
        Long userId,
        String name,
        Boolean enabled,
        List<SceneCommandDto> commands
) {}
