package org.example.userservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRequestTest {

    @Test
    void userRequest_ShouldHaveCorrectGettersAndSetters() {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("John Doe");
        userRequest.setEmail("john@example.com");
        userRequest.setAge(30);

        assertEquals("John Doe", userRequest.getName());
        assertEquals("john@example.com", userRequest.getEmail());
        assertEquals(30, userRequest.getAge());
    }

    @Test
    void userRequest_Constructor_ShouldSetFields() {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);

        assertEquals("John Doe", userRequest.getName());
        assertEquals("john@example.com", userRequest.getEmail());
        assertEquals(30, userRequest.getAge());
    }

    @Test
    void userRequest_EqualsAndHashCode_ShouldWorkCorrectly() {
        UserRequest user1 = new UserRequest("John Doe", "john@example.com", 30);
        UserRequest user2 = new UserRequest("John Doe", "john@example.com", 30);
        UserRequest user3 = new UserRequest("Jane Smith", "jane@example.com", 25);

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1.hashCode(), user3.hashCode());
    }

    @Test
    void userRequest_Validation_ShouldWork() {
        UserRequest userRequest = new UserRequest("Valid Name", "valid@example.com", 30);


        assertNotNull(userRequest.getName());
        assertNotNull(userRequest.getEmail());
        assertNotNull(userRequest.getAge());


        assertTrue(userRequest.getEmail().contains("@"));
    }
}