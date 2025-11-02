package gr.iccs.smart.mobility.user;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.util.EmptyJsonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Retrieve and manage users")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users", tags = { "Users" })
    public List<UserDTO> getAll() {
        return userService.getAll()
                .stream().map(UserDTO::fromUser)
                .toList();
    }

    @GetMapping(value = "/{username}")
    @Operation(summary = "Get single user", description = "Retrieve a user by their username", tags = { "Users" })
    public UserDTO getById(@PathVariable String username) {
        log.debug("User API: Get By ID");
        return UserDTO.fromUser(userService.getById(username));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user", description = "Create a new user", tags = { "Users" })
    public User create(@Valid @RequestBody UserDAO user) {
        log.info("Creating user {}", user);
        return userService.create(user.toUser());
    }

    @GetMapping("{username}/ride-status")
    @Operation(summary = "Get ride status", description = "Retrieve the status of a ride for a user", tags = { "Users" })
    public ResponseEntity<?> rideStatus(@PathVariable String username) {
        log.debug("User API: get ride status for {}", username);
        var status = userService.rideStatus(username);
        if (status.isEmpty()) {
            return new ResponseEntity<>(new EmptyJsonResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(status.get(), HttpStatus.OK);
    }
}
