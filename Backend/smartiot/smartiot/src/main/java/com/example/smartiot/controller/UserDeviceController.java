package com.example.smartiot.controller;

import com.example.smartiot.model.Device;
import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.service.UserDeviceService;
import com.example.smartiot.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Device", description = "Kullanıcıya atanmış cihaz işlemleri")
@RestController
@RequestMapping("/api/user-devices")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserDeviceController {

    private final UserDeviceService userDeviceService;
    private final UserService userService;

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

    // ======= Modelden otomatik atama (ünite göstermeden) =======
    @Operation(summary = "Modelden otomatik atama (ünite göstermeden)")
    @ApiResponse(responseCode = "201", description = "Atanan ünite ID",
            content = @Content(schema = @Schema(implementation = AssignResult.class)))
    @ApiResponse(responseCode = "400", description = "Eksik/Zorunlu alan hatası")
    @ApiResponse(responseCode = "409", description = "Stok bulunamadı ya da yarış durumu")
    @PostMapping("/assign-by-model")
    public ResponseEntity<?> assignByModel(
            @RequestBody(required = true,
                    content = @Content(schema = @Schema(implementation = AssignByModelDto.class),
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"userId\": 7,\n" +
                                    "  \"model\": \"Led-White\",\n" +
                                    "  \"userRoomId\": 12,\n" +
                                    "  \"assignedName\": \"Mutfak Beyaz\"\n" +
                                    "}")))
            @org.springframework.web.bind.annotation.RequestBody AssignByModelDto dto) {

        // ---- ZORUNLU ALAN KONTROLÜ ----
        if (dto == null
                || dto.getUserId() == null
                || dto.getModel() == null || dto.getModel().trim().isEmpty()
                || dto.getUserRoomId() == null
                || dto.getAssignedName() == null || dto.getAssignedName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("userId, model, userRoomId ve assignedName zorunludur.");
        }

        var result = userDeviceService.assignByModel(
                dto.getUserId(), dto.getModel().trim(), dto.getUserRoomId(), dto.getAssignedName().trim());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AssignResult(result.deviceId()));
    }

    @Data
    public static class AssignByModelDto {
        private Long userId;
        private String model;
        private Long userRoomId;   // ZORUNLU
        private String assignedName; // ZORUNLU
    }
    public record AssignResult(Long deviceId) {}

    // ======= Kullanıcının kullanabildiği üniteler =======
    @Operation(summary = "Kullanıcının kullanabildiği üniteler (allowed)")
    @GetMapping("/allowed/{userId}")
    public List<Device> allowed(@PathVariable Long userId) {
        return userDeviceService.listAllowedDevices(userId);
    }

    // ======= Eski uçlar (geri uyumluluk) =======
    @PutMapping("/{id}/status")
    public UserDevice updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        return userDeviceService.setActiveStatus(id, active);
    }

    // DEPRECATED: Eski çağrılar 'geçici' remove gibi davransın
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateUserDevice(@PathVariable Long id) {
        boolean success = userDeviceService.deactivateUserDevice(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ✅ YENİ: Remove – geçici/kalıcı
    // mode: temporary | replace
    @PatchMapping("/{id}/remove")
    public ResponseEntity<Void> remove(@PathVariable Long id,
                                       @RequestParam Long userId,
                                       @RequestParam String mode) {
        userDeviceService.removeUserDevice(id, userId, mode);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public void deleteUserDevice(@PathVariable Long id) {
        userDeviceService.deleteById(id);
    }

    @GetMapping
    public List<UserDevice> getAllUserDevices() {
        return userDeviceService.getAllUserDevices();
    }

    // ------- Eski "havuz" akışı (opsiyonel, geri uyumluluk için) -------
    @PostMapping("/register")
    public UserDevice registerOwned(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String deviceModel = body.get("deviceModel").toString();
        String deviceName  = body.get("deviceName").toString();
        String deviceUid   = body.get("deviceUid").toString();
        String alias       = body.getOrDefault("alias","").toString();
        return userDeviceService.registerOwnedDevice(userId, deviceModel, deviceName, deviceUid, alias);
    }

    @GetMapping("/pool/{userId}")
    public List<UserDevice> pool(@PathVariable Long userId) {
        return userDeviceService.getPool(userId);
    }

    @PutMapping("/{userDeviceId}/assign")
    public UserDevice assign(@PathVariable Long userDeviceId,
                             @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        Long userRoomId = Long.valueOf(body.get("userRoomId").toString());
        String assignedName = body.get("assignedName").toString();
        return userDeviceService.assignFromPool(userDeviceId, userId, userRoomId, assignedName);
    }

}
