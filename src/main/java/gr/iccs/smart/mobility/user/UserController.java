package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.usage.Used;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/people")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAll()
                .stream().map(UserDTO::fromUser)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/{username}")
    public UserDTO getById(@PathVariable String username) {
        return UserDTO.fromUser(userService.getById(username));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@Valid @RequestBody User user) {
        userService.create(user);
    }

    @PostMapping("{username}/ride")
    public void addRide(@PathVariable String username, @RequestBody Used useInfo) {
        userService.addRide(username, useInfo);
    }

//    @PostMapping("{username}/suggest")
//    public void suggestRoute(@PathVariable String username, @RequestBody ) {
//
//    }
}
