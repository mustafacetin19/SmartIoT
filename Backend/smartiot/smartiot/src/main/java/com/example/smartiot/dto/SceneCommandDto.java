package com.example.smartiot.dto;

public record SceneCommandDto(
        Long userDeviceId,
        String command,
        String value,
        Integer sortOrder
) {}
