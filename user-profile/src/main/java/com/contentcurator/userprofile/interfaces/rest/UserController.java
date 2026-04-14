package com.contentcurator.userprofile.interfaces.rest;
import com.contentcurator.userprofile.application.dto.UserResponse;
import com.contentcurator.userprofile.application.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication auth) {
        return ResponseEntity.ok(userService.getUser(auth.getName()));
    }
    @PostMapping("/me/interests")
    public ResponseEntity<Void> addInterest(Authentication auth, @RequestBody Map<String, String> body) {
        userService.addInterest(auth.getName(), body.get("topic"));
        return ResponseEntity.ok().build();
    }
}
