package com.sunlight.invest.controller;

import com.sunlight.invest.entity.User;
import com.sunlight.invest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
/**
 * 用户接口：提供CRUD操作
 */
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PostMapping
    public String createUser(@RequestBody User user) {
        int result = userService.insert(user);
        if (result > 0) {
            return "User created successfully";
        } else {
            return "Failed to create user";
        }
    }

    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        int result = userService.update(user);
        if (result > 0) {
            return "User updated successfully";
        } else {
            return "Failed to update user";
        }
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        int result = userService.deleteById(id);
        if (result > 0) {
            return "User deleted successfully";
        } else {
            return "Failed to delete user";
        }
    }
}