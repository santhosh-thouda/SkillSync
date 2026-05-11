package com.capgemini.group.controller;

import com.capgemini.group.entity.ContactMessage;
import com.capgemini.group.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/groups/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContactController {

    private final ContactMessageService service;

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitContactForm(@RequestBody ContactMessage message) {
        service.saveMessage(message);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Message stored successfully");
        return ResponseEntity.ok(response);
    }
}
