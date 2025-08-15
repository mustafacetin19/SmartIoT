package com.example.smartiot.controller;

import com.example.smartiot.model.Device;
import com.example.smartiot.repository.DeviceRepository;
import com.example.smartiot.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Device Management", description = "Katalog (salt-okunur) ve stok görünümü")
@RestController
@RequestMapping("/api/devices")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class DeviceManagementController {

    private final DeviceService deviceService;
    private final DeviceRepository deviceRepository;

    @Operation(summary = "Aktif katalog üniteleri")
    @ApiResponse(responseCode = "200", description = "Aktif cihazlar",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Device.class))))
    @GetMapping("/catalog")
    public List<Device> catalog() {
        return deviceService.findActive();
    }

    @Operation(summary = "Stok görünümü (model bazında)")
    @ApiResponse(responseCode = "200", description = "Model bazında stok")
    @GetMapping("/stock")
    public List<StockDto> stock() {
        return deviceRepository.stockByModel().stream()
                .map(r -> new StockDto(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        ((Number) r[2]).longValue()))
                .toList();
    }
    public record StockDto(String model, Long totalUnits, Long availableUnits) {}

    // Katalog CREATE/DELETE kapalı (admin dışı)
    @PostMapping
    public ResponseEntity<String> createDisabled() {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Device catalog is read-only.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDisabled(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body("Device catalog is read-only.");
    }

    // Aktiflik güncelleme istersen tutuldu (opsiyonel)
    @PutMapping("/{id}/status")
    public Device updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return deviceService.updateDeviceStatus(id, active);
    }
}
