package com.pegatron.maintenance.controller;

import com.pegatron.maintenance.model.User;
import com.pegatron.maintenance.repository.UserRepository;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setRole(updatedUser.getRole());

        // ⚠️ NO tocar password

        return userRepository.save(existingUser);
    }

//    @GetMapping("/me")
//    public User getCurrentUser(Authentication authentication) {
//        if (authentication == null) return null;
//        // Buscamos al usuario en la base de datos por el nombre de usuario de la sesión
//        return userRepository.findByUsername(authentication.getName())
//                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
//    }

}