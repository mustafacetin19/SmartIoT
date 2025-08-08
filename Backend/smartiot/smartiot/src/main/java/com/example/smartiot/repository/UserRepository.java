package com.example.smartiot.repository;

import com.example.smartiot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tüm kullanıcılar arasından email ile bulur (aktiflik fark etmeksizin)
    Optional<User> findByEmail(String email);

    // Sadece aktif kullanıcıyı email ile bulur (login için kullanışlı)
    Optional<User> findByEmailAndActiveTrue(String email);

    // Sadece aktif kullanıcıları getir (listeleme ekranı için kullanışlı)
    List<User> findAllByActiveTrue();


}
