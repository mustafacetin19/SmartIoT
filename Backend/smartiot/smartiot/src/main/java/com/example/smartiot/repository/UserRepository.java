package com.example.smartiot.repository;

import com.example.smartiot.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndActiveTrue(String email);

    List<User> findAllByActiveTrue();

    @Modifying
    @Query("update User u set u.active=false where u.id=:id")
    int deactivateById(@Param("id") Long id);
}
