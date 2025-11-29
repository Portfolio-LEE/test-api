package org.example.traffictest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/test")
    public Map<String, Object> perfTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OK");
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }
}