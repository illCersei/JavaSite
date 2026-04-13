package cersei.testapi.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class Controller {

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        log.info("Hello endpoint called");
        return ResponseEntity.ok("Hello World");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> admin(){
        log.info("Admin endpoint called");
        return ResponseEntity.ok("Вы одмен");
    }

    @GetMapping("/public/ping")
    public ResponseEntity<String> ping(){
        log.info("Received ping request");
        return ResponseEntity.ok("pong");
    }
}
