package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.userservice.dto.UserRequest;
import org.example.userservice.dto.UserResource;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    @Operation(summary = "Health check", description = "Check if user service is running")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("User Service is running on port 8080");
    }

    @Operation(summary = "Create a new user", description = "Create a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping
    public ResponseEntity<EntityModel<UserResource>> createUser(
            @Parameter(description = "User data to create", required = true)
            @Valid @RequestBody UserRequest userRequest) {

        UserResponse userResponse = userService.createUser(userRequest);
        UserResource userResource = convertToResource(userResponse);


        EntityModel<UserResource> resource = EntityModel.of(userResource);
        resource.add(linkTo(methodOn(UserController.class).getUserById(userResource.getId())).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(userResource.getId(), userRequest)).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(userResource.getId())).withRel("delete"));

        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a user by their unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResource>> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {

        UserResponse userResponse = userService.getUserById(id);
        UserResource userResource = convertToResource(userResponse);


        EntityModel<UserResource> resource = EntityModel.of(userResource);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, new UserRequest())).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));
        resource.add(linkTo(methodOn(UserController.class).getUserByEmail(userResource.getEmail())).withRel("by-email"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponse(responseCode = "200", description = "List of users retrieved successfully")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<UserResource>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();


        List<EntityModel<UserResource>> userResources = users.stream()
                .map(this::convertToResource)
                .map(userResource -> {
                    EntityModel<UserResource> resource = EntityModel.of(userResource);
                    resource.add(linkTo(methodOn(UserController.class).getUserById(userResource.getId())).withSelfRel());
                    resource.add(linkTo(methodOn(UserController.class).updateUser(userResource.getId(), new UserRequest())).withRel("update"));
                    resource.add(linkTo(methodOn(UserController.class).deleteUser(userResource.getId())).withRel("delete"));
                    return resource;
                })
                .collect(Collectors.toList());


        CollectionModel<EntityModel<UserResource>> collection = CollectionModel.of(userResources);
        collection.add(linkTo(methodOn(UserController.class).getAllUsers()).withSelfRel());
        collection.add(linkTo(methodOn(UserController.class).createUser(new UserRequest())).withRel("create-user"));

        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Update user", description = "Update an existing user's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResource>> updateUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated user data", required = true)
            @Valid @RequestBody UserRequest userRequest) {

        UserResponse userResponse = userService.updateUser(id, userRequest);
        UserResource userResource = convertToResource(userResponse);

        EntityModel<UserResource> resource = EntityModel.of(userResource);
        resource.add(linkTo(methodOn(UserController.class).getUserById(id)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));
        resource.add(linkTo(methodOn(UserController.class).updateUser(id, userRequest)).withRel("update"));
        resource.add(linkTo(methodOn(UserController.class).deleteUser(id)).withRel("delete"));

        return ResponseEntity.ok(resource);
    }

    @Operation(summary = "Delete user", description = "Delete a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long id) {

        userService.deleteUser(id);

        Link allUsersLink = linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users");

        return ResponseEntity.noContent()
                .header("Link", allUsersLink.toString())
                .build();
    }

    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResource.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<EntityModel<UserResource>> getUserByEmail(
            @Parameter(description = "User email", required = true, example = "user@example.com")
            @PathVariable String email) {

        UserResponse userResponse = userService.getUserByEmail(email);
        UserResource userResource = convertToResource(userResponse);

        // HATEOAS links
        EntityModel<UserResource> resource = EntityModel.of(userResource);
        resource.add(linkTo(methodOn(UserController.class).getUserByEmail(email)).withSelfRel());
        resource.add(linkTo(methodOn(UserController.class).getUserById(userResource.getId())).withRel("by-id"));
        resource.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("all-users"));

        return ResponseEntity.ok(resource);
    }

    private UserResource convertToResource(UserResponse userResponse) {
        return new UserResource(
                userResponse.getId(),
                userResponse.getName(),
                userResponse.getEmail(),
                userResponse.getAge(),
                userResponse.getCreatedAt() != null ? userResponse.getCreatedAt().toString() : null
        );
    }
}