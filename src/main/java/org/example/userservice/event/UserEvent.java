package org.example.userservice.event;

import java.time.LocalDateTime;

public class UserEvent {
    private String eventType; // CREATED, DELETED
    private String email;
    private LocalDateTime timestamp;
    private Long userId;

    public UserEvent() {
    }

    public UserEvent(String eventType, String email, Long userId) {
        this.eventType = eventType;
        this.email = email;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventType='" + eventType + '\'' +
                ", email='" + email + '\'' +
                ", timestamp=" + timestamp +
                ", userId=" + userId +
                '}';
    }
}