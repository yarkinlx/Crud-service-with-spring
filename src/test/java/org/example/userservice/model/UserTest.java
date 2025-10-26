package org.example.userservice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void user_ShouldHaveCorrectGettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setAge(30);

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(30, user.getAge());
        assertEquals(now, user.getCreatedAt());
    }

    @Test
    void user_Constructor_ShouldSetFields() {
        User user = new User("John Doe", "john@example.com", 30);

        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(30, user.getAge());
    }

    @Test
    void user_EqualsAndHashCode_ShouldWorkCorrectly() {
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);

        User user2 = new User("John Doe", "john@example.com", 30);
        user2.setId(1L);

        User user3 = new User("Jane Smith", "jane@example.com", 25);
        user3.setId(2L);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void user_ToString_ShouldContainAllFields() {
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);

        String toString = user.toString();

        assertTrue(toString.contains("John Doe"));
        assertTrue(toString.contains("john@example.com"));
        assertTrue(toString.contains("30"));
        assertTrue(toString.contains("1"));
    }

    @Test
    void user_OnCreate_ShouldSetCreatedAt() {
        User user = new User("John Doe", "john@example.com", 30);

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertTrue(user.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void user_NoArgsConstructor_ShouldCreateEmptyUser() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getAge());
        assertNull(user.getCreatedAt());
    }
}