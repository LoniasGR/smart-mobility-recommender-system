package gr.iccs.smart.mobility.user;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import gr.iccs.smart.mobility.usage.UseDTO;
import gr.iccs.smart.mobility.util.EmptyJsonResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/people")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> getAll() {
        log.debug("User API: Get All");
        return userService.getAll()
                .stream().map(UserDTO::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{username}")
    public UserDTO getById(@PathVariable String username) {
        log.debug("User API: Get By ID");
        return UserDTO.fromUser(userService.getById(username));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.debug("User API: Create {}", user);
        return userService.create(user);
    }

    @PostMapping("{username}/ride")
    public void rideManagement(@PathVariable String username, @RequestBody UseDTO useInfo) {
        log.debug("User API: add ride for user {}with ride data {}", username, useInfo);
        userService.manageRide(username, useInfo);
    }

    @GetMapping("{username}/ride-status")
    public ResponseEntity<?> rideStatus(@PathVariable String username) {
        log.debug("User API: get ride status for {}", username);
        var status = userService.rideStatus(username);
        if (status.isEmpty()) {
            return new ResponseEntity<>(new EmptyJsonResponse(), HttpStatus.OK);
        }
        return new ResponseEntity<>(status.get(), HttpStatus.OK);
    }
}
