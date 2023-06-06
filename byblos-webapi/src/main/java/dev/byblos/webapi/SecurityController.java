package dev.byblos.webapi;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
public class SecurityController {
    @GetMapping("/api/v1/user")
    public Map<String, Object> user(Principal principal, Authentication auth) {
        return Map.of("username", auth.getName());
    }
}