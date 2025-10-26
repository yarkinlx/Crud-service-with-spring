package org.example.userservice.service;

import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.exception.DuplicateEmailException;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.model.User; // Добавьте этот импорт
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldSuccessfullyCreateUser() {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(1L);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.createUser(userRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        assertEquals(30, result.getAge());

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        UserRequest userRequest = new UserRequest("John Doe", "john@example.com", 30);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(userRequest));

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithExistingId_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(userId);
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("John Doe", result.getName());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowException() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", 25);
        user2.setId(2L);

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_ShouldSuccessfullyUpdateUser() {
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("John Updated", "john.updated@example.com", 31);

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        User updatedUser = new User("John Updated", "john.updated@example.com", 31);
        updatedUser.setId(userId);
        updatedUser.setCreatedAt(existingUser.getCreatedAt());

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailAndIdNot("john.updated@example.com", userId)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse result = userService.updateUser(userId, userRequest);

        assertNotNull(result);
        assertEquals("John Updated", result.getName());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals(31, result.getAge());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmailAndIdNot("john.updated@example.com", userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldSuccessfullyDeleteUser() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }
}