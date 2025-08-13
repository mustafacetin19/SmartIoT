package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.service.UserDeviceService;
import com.example.smartiot.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Device", description = "Kullanıcıya atanmış cihaz işlemleri")
@RestController
@RequestMapping("/api/user-devices")
@CrossOrigin(origins = "http://localhost:3000")
public class UserDeviceController {

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Kullanıcının cihazlarını listele")
    @ApiResponse(responseCode = "200", description = "Başarılı",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDevice.class))))
    @GetMapping("/user/{userId}")
    public List<UserDevice> getUserDevices(
            @Parameter(description = "Kullanıcı ID", example = "7") @PathVariable Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return userDeviceService.getDevicesByUser(user);
    }

    @Operation(summary = "Kullanıcı cihazı aktif/pasif durumunu güncelle")
    @ApiResponse(responseCode = "200", description = "Güncellenen kayıt",
            content = @Content(schema = @Schema(implementation = UserDevice.class)))
    @PutMapping("/{id}/status")
    public UserDevice updateStatus(
            @Parameter(description = "UserDevice ID", example = "15") @PathVariable Long id,
            @Parameter(description = "Aktif mi?", example = "true") @RequestParam boolean active) {
        return userDeviceService.setActiveStatus(id, active);
    }

    @Operation(summary = "Kullanıcı cihazını pasifleştir")
    @ApiResponse(responseCode = "200", description = "Pasifleştirildi")
    @ApiResponse(responseCode = "404", description = "Kayıt bulunamadı")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUserDevice(
            @Parameter(description = "UserDevice ID", example = "15") @PathVariable Long id) {
        boolean success = userDeviceService.deactivateUserDevice(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Kullanıcı cihazını sil")
    @ApiResponse(responseCode = "200", description = "Silindi")
    @DeleteMapping("/{id}")
    public void deleteUserDevice(
            @Parameter(description = "UserDevice ID", example = "15") @PathVariable Long id) {
        userDeviceService.deleteById(id);
    }

    @Operation(summary = "Tüm kullanıcı cihazlarını listele")
    @ApiResponse(responseCode = "200", description = "Başarılı",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDevice.class))))
    @GetMapping
    public List<UserDevice> getAllUserDevices() {
        return userDeviceService.getAllUserDevices();
    }

    // ===================== ESKİ HAVUZ AKIŞI =====================

    @Operation(summary = "Kullanıcıya sahip olunan cihaz kaydet (havuz mantığı)")
    @ApiResponse(responseCode = "200", description = "Kayıt başarıyla oluşturuldu",
            content = @Content(schema = @Schema(implementation = UserDevice.class),
                    examples = @ExampleObject(
                            value = "{\"id\":31,\"user\":{\"id\":7},\"device\":{\"id\":101},\"assignedName\":\"Salon Lamba\",\"active\":true}")))
    @PostMapping("/register")
    public UserDevice registerOwned(
            @RequestBody(description = "Kayıt gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"userId\": 7,\n" +
                                    "  \"deviceModel\": \"LED_WS2812B\",\n" +
                                    "  \"deviceName\": \"Salon Lamba\",\n" +
                                    "  \"deviceUid\": \"ESP32-3F:AB:9C:11\",\n" +
                                    "  \"alias\": \"lamba1\"\n" +
                                    "}")))
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String deviceModel = body.get("deviceModel").toString();
        String deviceName  = body.get("deviceName").toString();
        String deviceUid   = body.get("deviceUid").toString();
        String alias       = body.getOrDefault("alias","").toString();
        return userDeviceService.registerOwnedDevice(userId, deviceModel, deviceName, deviceUid, alias);
    }

    @Operation(summary = "Kullanıcının cihaz havuzunu getir")
    @ApiResponse(responseCode = "200", description = "Havuz listesi",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDevice.class))))
    @GetMapping("/pool/{userId}")
    public List<UserDevice> pool(
            @Parameter(description = "Kullanıcı ID", example = "7") @PathVariable Long userId) {
        return userDeviceService.getPool(userId);
    }

    // Havuzdan odaya: havuz item'ı korunur, kopyası odaya eklenir
    @Operation(summary = "Havuzdaki cihazı odaya ata (kopya oluşturur)")
    @ApiResponse(responseCode = "200", description = "Atama başarılı",
            content = @Content(schema = @Schema(implementation = UserDevice.class)))
    @PutMapping("/{userDeviceId}/assign")
    public UserDevice assign(
            @Parameter(description = "Havuzdaki UserDevice ID", example = "31") @PathVariable Long userDeviceId,
            @RequestBody(description = "Atama gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"userId\": 7,\n" +
                                    "  \"userRoomId\": 12,\n" +
                                    "  \"assignedName\": \"Salon Lamba 1\"\n" +
                                    "}")))
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long userRoomId = Long.valueOf(body.get("userRoomId").toString());
        String assignedName = body.get("assignedName").toString();
        return userDeviceService.assignFromPool(userDeviceId, userId, userRoomId, assignedName);
    }

    // ===================== YENİ: KATALOGTAN DOĞRUDAN EKLE =====================

    @Operation(summary = "Katalogtan doğrudan odaya cihaz ekle")
    @ApiResponse(responseCode = "200", description = "Ekleme başarılı",
            content = @Content(schema = @Schema(implementation = UserDevice.class)))
    @PostMapping("/add")
    public UserDevice addFromCatalog(
            @RequestBody(description = "Ekleme gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"userId\": 7,\n" +
                                    "  \"deviceId\": 101,\n" +
                                    "  \"userRoomId\": 12,\n" +
                                    "  \"assignedName\": \"Salon Lamba\"\n" +
                                    "}")))
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId     = Long.valueOf(body.get("userId").toString());
        Long deviceId   = Long.valueOf(body.get("deviceId").toString());    // devices.id
        Long userRoomId = Long.valueOf(body.get("userRoomId").toString());  // user_room.id
        String assigned = body.getOrDefault("assignedName", "").toString();
        return userDeviceService.addDeviceToRoom(userId, deviceId, userRoomId, assigned);
    }
}
