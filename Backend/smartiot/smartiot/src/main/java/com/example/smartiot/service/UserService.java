package com.example.smartiot.service;

import com.example.smartiot.model.User;
import com.example.smartiot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User register(User user) {
        user.setActive(true); // Yeni kullanıcı aktif olarak kaydedilir
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.filter(u ->
                u.isActive() && u.getPassword().equals(password)
        );
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deactivateUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
    }

    // ✅ Kullanıcıyı ID ile getir
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
