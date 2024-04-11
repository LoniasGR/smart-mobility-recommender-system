package gr.iccs.smart.mobility.user;

import gr.iccs.smart.mobility.usage.Used;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/people")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Iterable<User> getAll() {
        return userService.getAll();
    }

    @GetMapping(value = "/{username}")
    public User getById(@PathVariable String username) {
        return userService.getById(username);
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
}
