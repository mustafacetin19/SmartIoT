package com.example.smartiot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "scene_commands")
public class SceneCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scene_id", nullable = false)
    private Long sceneId;

    @Column(name = "user_device_id", nullable = false)
    private Long userDeviceId;

    @Column(length = 32, nullable = false)
    private String command;

    @Column(length = 64)
    private String value;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }
    public Long getUserDeviceId() { return userDeviceId; }
    public void setUserDeviceId(Long userDeviceId) { this.userDeviceId = userDeviceId; }
    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
