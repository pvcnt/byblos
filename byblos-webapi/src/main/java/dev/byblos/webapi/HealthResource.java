package dev.byblos.webapi;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public final class HealthResource {

    @GetMapping(value = "/health", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("OK");
    }
}
