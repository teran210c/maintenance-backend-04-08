package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.dto.UserDTO;
import com.pegatron.maintenance.model.User;
import com.pegatron.maintenance.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserDTO login(@RequestBody User request) {

        Optional<User> user =
                userRepository.findByUsername(request.getUsername());

        if (user.isPresent()
                && user.get().getPassword().equals(request.getPassword())) {

            UserDTO dto = new UserDTO();
            dto.setId(user.get().getId());
            dto.setUsername(user.get().getUsername());
            dto.setRole(user.get().getRole());

            return dto;
        }

        throw new RuntimeException("Invalid credentials");
    }
}