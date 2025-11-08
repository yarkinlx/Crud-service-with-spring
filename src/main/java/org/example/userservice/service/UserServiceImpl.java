package org.example.userservice.service;

import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.event.UserEvent;
import org.example.userservice.exception.DuplicateEmailException;
import org.example.userservice.exception.ResourceNotFoundException;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEventPublisher userEventPublisher;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserEventPublisher userEventPublisher) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        validateEmailUniqueness(userRequest.getEmail());

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User savedUser = userRepository.save(user);

        UserEvent userEvent = new UserEvent("CREATED", savedUser.getEmail(), savedUser.getId());
        userEventPublisher.publishUserEvent(userEvent);

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        User user = findUserById(id);

        if (!user.getEmail().equals(userRequest.getEmail())) {
            validateEmailUniquenessForOtherUser(userRequest.getEmail(), id);
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setAge(userRequest.getAge());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUserById(id);
        String userEmail = user.getEmail();

        userRepository.deleteById(id);

        UserEvent userEvent = new UserEvent("DELETED", userEmail, id);
        userEventPublisher.publishUserEvent(userEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
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