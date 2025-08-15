package com.example.smartiot.controller;

import com.example.smartiot.model.UserRoom;
import com.example.smartiot.service.UserRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "User Room", description = "Kullanıcı odaları")
@RestController
@RequestMapping("/api/user-rooms")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class UserRoomController {

    private final UserRoomService service;

    @Operation(summary = "Kullanıcının aktif odalarını listele")
    @ApiResponse(responseCode = "200", description = "Aktif oda listesi",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserRoom.class))))
    @GetMapping("/by-user/{userId}")
    public List<UserRoom> list(@PathVariable Long userId) {
        return service.listActiveRooms(userId);
    }

    @Operation(summary = "Oda oluştur")
    @ApiResponse(responseCode = "200", description = "Oluşturulan oda",
            content = @Content(schema = @Schema(implementation = UserRoom.class)))
    @PostMapping
    public UserRoom create(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(body.get("userId").toString());
        String roomName = body.get("roomName").toString();
        return service.create(userId, roomName);
    }

    @Operation(summary = "Odayı pasifleştir")
    @ApiResponse(responseCode = "200", description = "Pasifleştirildi")
    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id, @RequestParam Long userId) {
        service.deactivate(userId, id);
    }
}
