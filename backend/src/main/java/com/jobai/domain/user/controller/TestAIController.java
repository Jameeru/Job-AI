package com.jobai.domain.user.controller;

import com.jobai.infrastructure.ai.BedrockAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/test-ai")
@RequiredArgsConstructor
public class TestAIController {

    private final BedrockAIClient bedrockAIClient;

    @GetMapping
    public ResponseEntity<Map<String, String>> testClaude() {
        try {
            String prompt = "Hello! Please reply with 'Claude is working!' and nothing else.";
            String response = bedrockAIClient.invoke(prompt);
            
            Map<String, String> result = new HashMap<>();
            result.put("status", "success");
            result.put("response", response);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }
}
