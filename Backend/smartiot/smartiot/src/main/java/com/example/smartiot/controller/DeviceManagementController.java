package com.example.smartiot.controller;

import com.example.smartiot.model.Device;
import com.example.smartiot.service.DeviceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Device Management", description = "Cihaz havuzu ve yönetim")
@RestController
@RequestMapping("/api/device")
@CrossOrigin(origins = "http://localhost:3000")
public class DeviceManagementController {

    @Autowired
    private DeviceService deviceService;

    @Operation(summary = "Tüm cihazları (katalog) listele")
    @ApiResponse(responseCode = "200", description = "Cihaz listesi",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = Device.class))))
    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @Operation(summary = "Kataloğa yeni cihaz ekle")
    @ApiResponse(responseCode = "200", description = "Oluşturulan cihaz",
            content = @Content(schema = @Schema(implementation = Device.class)))
    @PostMapping
    public Device createDevice(
            @RequestBody(description = "Cihaz gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Device.class),
                            examples = @ExampleObject(value = "{\"deviceUid\":\"ESP32-NEW\",\"deviceName\":\"Yeni Cihaz\",\"deviceModel\":\"LED_WS2812B\",\"active\":true}")))
            @org.springframework.web.bind.annotation.RequestBody Device device) {
        return deviceService.saveDevice(device);
    }

    @Operation(summary = "Katalogdan cihaz sil")
    @ApiResponse(responseCode = "200", description = "Silindi")
    @DeleteMapping("/{id}")
    public void deleteDevice(
            @Parameter(description = "Device ID", example = "101") @PathVariable Long id) {
        deviceService.deleteDevice(id);
    }

    @Operation(summary = "Katalogdaki cihazın aktiflik durumunu güncelle")
    @ApiResponse(responseCode = "200", description = "Güncellenen kayıt",
            content = @Content(schema = @Schema(implementation = Device.class)))
    @PutMapping("/{id}/status")
    public Device updateDeviceStatus(
            @Parameter(description = "Device ID", example = "101") @PathVariable Long id,
            @Parameter(description = "Aktif mi?", example = "true") @RequestParam boolean active) {
        return deviceService.updateDeviceStatus(id, active);
    }
}
