package com.contentcurator.userprofile.interfaces.rest;
import com.contentcurator.userprofile.application.dto.RegisterUserCommand;
import com.contentcurator.userprofile.application.dto.UserResponse;
import com.contentcurator.userprofile.application.port.out.UserRepository;
import com.contentcurator.userprofile.application.service.UserService;
import com.contentcurator.userprofile.infrastructure.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserCommand cmd) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(cmd));
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        return userRepository.findByEmail(credentials.get("email"))
            .filter(u -> passwordEncoder.matches(credentials.get("password"), u.getPasswordHash()))
            .map(u -> ResponseEntity.ok(Map.of("token", jwtService.generateToken(u.getId(), u.getEmail()), "userId", u.getId())))
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
