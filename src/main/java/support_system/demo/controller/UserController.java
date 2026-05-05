package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import support_system.demo.model.User;
import support_system.demo.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService service;

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User saved = service.register(user);
            saved.setPassword(null);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration failed. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ✅ LOGIN (JWT RESPONSE)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User validated = service.authenticate(user.getEmail(), user.getPassword());
            String token = service.generateToken(validated.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login Successful");
            response.put("token", token);
            response.put("email", validated.getEmail());
            response.put("role", validated.getRole());
            response.put("name", validated.getName());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ✅ CURRENT USER INFO
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "").trim();
            String email = service.extractEmailFromToken(token);
            User user = service.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ✅ LIST AVAILABLE THERAPISTS
    @GetMapping("/therapists")
    public ResponseEntity<?> getTherapists() {
        java.util.List<User> therapists = service.getTherapists();
        java.util.List<Map<String, String>> response = therapists.stream()
                .map(user -> {
                    Map<String, String> therapistInfo = new HashMap<>();
                    therapistInfo.put("name", user.getName());
                    therapistInfo.put("email", user.getEmail());
                    return therapistInfo;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}