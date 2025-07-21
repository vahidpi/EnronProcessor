package org.enronprocessor.enronprocessor.controller;

import org.enronprocessor.enronprocessor.service.EnronEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enron")
public class EnronEmailController {
    private final EnronEmailService emailService;

    public EnronEmailController(EnronEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startIngestion() {
        try {
            emailService.startIngestion();
            return ResponseEntity.ok("Ingestion started.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred.");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> isIngestionFinished() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("finished", emailService.isIngestionFinished());
            status.put("messagesProcessed", emailService.getTotalMessages());
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unexpected error while checking status.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/top-senders")
    public ResponseEntity<?> getTopSenders() {
        try {
            List<Map.Entry<String, Integer>> topSenders = emailService.getTopSenders();
            return ResponseEntity.ok(topSenders);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Unexpected error while retrieving top senders.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
