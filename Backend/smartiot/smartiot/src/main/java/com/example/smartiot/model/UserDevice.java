package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices")
@Getter @Setter @ToString @NoArgsConstructor @AllArgsConstructor
public class UserDevice {

    public enum Status { ACTIVE, INACTIVE_RESERVED, RETIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // users.user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    // devices.device_id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Device device;

    // user_room_id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_room_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserRoom userRoom;

    @Column(name = "assigned_name")
    private String assignedName;

    @Column(name = "active")
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;
}
