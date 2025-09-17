package com.example.smartiot.dto;

import java.util.List;

public record SceneUpdateRequest(
        String name,
        Boolean enabled,
        List<SceneCommandDto> commands
) {}
