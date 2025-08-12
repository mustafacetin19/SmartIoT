package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.repository.UserRepository;
import com.example.smartiot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    // Kayıt
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("E-mail is already in use");
        }
        user.setActive(true);
        User savedUser = userService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
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

    // ✅ Güvenli pasif etme: user → rooms → user_devices → devices
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        userService.deactivateUser(id);
        return ResponseEntity.ok("✅ User, rooms, user_devices and related devices deactivated (safe mode).");
    }
}
