package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.repository.UserRepository;
import com.example.smartiot.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "User", description = "Kullanıcı kayıt/giriş ve pasifleştirme")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Kayıt ol")
    @ApiResponse(responseCode = "200", description = "Kayıt başarılı",
            content = @Content(schema = @Schema(implementation = User.class),
                    examples = @ExampleObject(
                            value = "{\"id\":7,\"email\":\"user@example.com\",\"password\":\"123456\",\"firstName\":\"Mustafa\",\"lastName\":\"Çetin\",\"active\":true}")))
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody(description = "Kayıt gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(
                                    value = "{\"email\":\"user@example.com\",\"password\":\"123456\",\"firstName\":\"Mustafa\",\"lastName\":\"Çetin\"}")))
            @org.springframework.web.bind.annotation.RequestBody User user) {
        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("E-mail is already in use");
        }
        user.setActive(true);
        User savedUser = userService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    @Operation(summary = "Giriş yap")
    @ApiResponse(responseCode = "200", description = "Giriş başarılı",
            content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgisi",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "Invalid credentials")))
    @ApiResponse(responseCode = "403", description = "Kullanıcı pasif",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "User is deactivated. Please create a new account.")))
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody(description = "Giriş gövdesi",
                    required = true,
                    content = @Content(schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(value = "{\"email\":\"user@example.com\",\"password\":\"123456\"}")))
            @org.springframework.web.bind.annotation.RequestBody User user) {
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());
        if (foundUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        User u = foundUser.get();
        if (!u.isActive()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User is deactivated. Please create a new account.");
        }
        if (!u.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        return ResponseEntity.ok(u);
    }

    @Operation(summary = "Kullanıcıyı güvenli şekilde pasifleştir (rooms, user_devices, devices)")
    @ApiResponse(responseCode = "200", description = "Pasifleştirme başarılı",
            content = @Content(mediaType = "text/plain",
                    examples = @ExampleObject(value = "✅ User, rooms, user_devices and related devices deactivated (safe mode).")))
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @Parameter(description = "Kullanıcı ID", example = "7") @PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.deactivateUser(id);
        return ResponseEntity.ok("✅ User, rooms, user_devices and related devices deactivated (safe mode).");
    }
}
