package com.example.smartiot.model;

public class ServoCommand {
    private String servo;
    private int angle;

    public String getServo() {
        return servo;
    }

    public void setServo(String servo) {
        this.servo = servo;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }
}
