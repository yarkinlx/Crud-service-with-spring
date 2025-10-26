package org.example.userservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseTest {

    @Test
    void userResponse_ShouldHaveCorrectGettersAndSetters() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse userResponse = new UserResponse();
        userResponse.setId(1L);
        userResponse.setName("John Doe");
        userResponse.setEmail("john@example.com");
        userResponse.setAge(30);
        userResponse.setCreatedAt(now);

        assertEquals(1L, userResponse.getId());
        assertEquals("John Doe", userResponse.getName());
        assertEquals("john@example.com", userResponse.getEmail());
        assertEquals(30, userResponse.getAge());
        assertEquals(now, userResponse.getCreatedAt());
    }

    @Test
    void userResponse_Constructor_ShouldSetFields() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse userResponse = new UserResponse(1L, "John Doe", "john@example.com", 30, now);

        assertEquals(1L, userResponse.getId());
        assertEquals("John Doe", userResponse.getName());
        assertEquals("john@example.com", userResponse.getEmail());
        assertEquals(30, userResponse.getAge());
        assertEquals(now, userResponse.getCreatedAt());
    }

    @Test
    void userResponse_EqualsAndHashCode_ShouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        UserResponse user1 = new UserResponse(1L, "John Doe", "john@example.com", 30, now);
        UserResponse user2 = new UserResponse(1L, "John Doe", "john@example.com", 30, now);
        UserResponse user3 = new UserResponse(2L, "Jane Smith", "jane@example.com", 25, now);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void userResponse_AllArgsConstructor_ShouldSetAllFields() {
        LocalDateTime createdAt = LocalDateTime.now();
        UserResponse response = new UserResponse(1L, "John", "john@test.com", 30, createdAt);

        assertEquals(1L, response.getId());
        assertEquals("John", response.getName());
        assertEquals("john@test.com", response.getEmail());
        assertEquals(30, response.getAge());
        assertEquals(createdAt, response.getCreatedAt());
    }
}