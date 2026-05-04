package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import support_system.demo.model.User;
import support_system.demo.repository.UserRepository;
import support_system.demo.security.JwtUtil;

import java.util.Optional;

@Service
public class UserService {

    private final JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    UserService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ REGISTER USER
    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // ✅ LOGIN USER (UNCHANGED STRUCTURE)
    public String login(String email, String password) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {

            if (passwordEncoder.matches(password, user.get().getPassword())) {

                return jwtUtil.generateToken(email);

            } else {
                return "Invalid Password";
            }

        } else {
            return "User not found";
        }
    }

    public User authenticate(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        if (!passwordEncoder.matches(password, user.get().getPassword())) {
            throw new IllegalArgumentException("Invalid Password");
        }
        return user.get();
    }

    public String generateToken(String email) {
        return jwtUtil.generateToken(email);
    }

    public String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public java.util.List<User> getTherapists() {
        return userRepository.findByRole("THERAPIST");
    }
}
