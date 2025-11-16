package org.example.userservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.event.UserEvent;
import org.example.userservice.exception.DuplicateEmailException;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           UserEventPublisher userEventPublisher,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "createUserFallback")
    public UserResponse createUser(UserRequest userRequest) {
        logger.info("Creating user with email: {}", userRequest.getEmail());

        validateEmailUniqueness(userRequest.getEmail());

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User savedUser = userRepository.save(user);

        UserEvent userEvent = new UserEvent("CREATED", savedUser.getEmail(), savedUser.getId());
        userEventPublisher.publishUserEvent(userEvent);

        logger.info("User created successfully with ID: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    public UserResponse createUserFallback(UserRequest userRequest, Throwable throwable) {
        logger.error("Fallback method called for createUser due to: {}", throwable.getMessage());
        throw new RuntimeException("User service is temporarily unavailable. Please try again later.");
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(Long id) {
        logger.info("Fetching user by ID: {}", id);
        User user = findUserById(id);
        return mapToUserResponse(user);
    }

    public UserResponse getUserByIdFallback(Long id, Throwable throwable) {
        logger.error("Fallback method called for getUserById due to: {}", throwable.getMessage());
        throw new ResourceNotFoundException("User service is temporarily unavailable");
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "userService", fallbackMethod = "getAllUsersFallback")
    public List<UserResponse> getAllUsers() {
        logger.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public List<UserResponse> getAllUsersFallback(Throwable throwable) {
        logger.error("Fallback method called for getAllUsers due to: {}", throwable.getMessage());
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "updateUserFallback")
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        logger.info("Updating user with ID: {}", id);
        User user = findUserById(id);

        if (!user.getEmail().equals(userRequest.getEmail())) {
            validateEmailUniquenessForOtherUser(userRequest.getEmail(), id);
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", id);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse updateUserFallback(Long id, UserRequest userRequest, Throwable throwable) {
        logger.error("Fallback method called for updateUser due to: {}", throwable.getMessage());
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "deleteUserFallback")
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        User user = findUserById(id);
        String userEmail = user.getEmail();

        userRepository.deleteById(id);

        UserEvent userEvent = new UserEvent("DELETED", userEmail, id);
        userEventPublisher.publishUserEvent(userEvent);
        logger.info("User deleted successfully with ID: {}", id);
    }

    public void deleteUserFallback(Long id, Throwable throwable) {
        logger.error("Fallback method called for deleteUser due to: {}", throwable.getMessage());
        throw new RuntimeException("User service is temporarily unavailable");
    }

    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserResponse getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public UserResponse getUserByEmailFallback(String email, Throwable throwable) {
        logger.error("Fallback method called for getUserByEmail due to: {}", throwable.getMessage());
        throw new ResourceNotFoundException("User service is temporarily unavailable");
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("User with email " + email + " already exists");
        }
    }

    private void validateEmailUniquenessForOtherUser(String email, Long excludeUserId) {
        if (userRepository.existsByEmailAndIdNot(email, excludeUserId)) {
            throw new DuplicateEmailException("Another user with email " + email + " already exists");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAge(user.getAge());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}