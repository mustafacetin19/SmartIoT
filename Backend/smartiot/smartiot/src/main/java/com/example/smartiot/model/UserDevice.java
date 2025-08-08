package com.example.smartiot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Device device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Room room;

    @Column(name = "assigned_name")
    private String assignedName;

    @Column(name = "active")
    private boolean active = true;
}
