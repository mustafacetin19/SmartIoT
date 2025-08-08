package com.example.smartiot.controller;

import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.repository.UserDeviceRepository;
import com.example.smartiot.repository.UserRepository;
import com.example.smartiot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    // ✅ Kayıt olma
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        Optional<User> existingUser = userService.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(400).body("E-mail is already in use");
        }

        user.setActive(true); // ✅ Yeni kullanıcıyı aktif yap
        User savedUser = userService.register(user);
        return ResponseEntity.ok(savedUser);
    }

    // ✅ Giriş yapma (aktif kontrolü dahil)
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

    // ✅ Hesap pasifleştirme (kullanıcı ve cihazları)
    @PutMapping("/users/{id}/deactivate")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setActive(false);
        userRepository.save(user);

        List<UserDevice> userDevices = userDeviceRepository.findByUserId(id);
        for (UserDevice ud : userDevices) {
            ud.setActive(false);
        }
        userDeviceRepository.saveAll(userDevices);

        return ResponseEntity.ok("✅ User and their devices have been deactivated.");
    }

}
