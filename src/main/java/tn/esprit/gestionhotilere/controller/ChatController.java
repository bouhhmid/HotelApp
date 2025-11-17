package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.service.ChatHotelService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ChatController {

    private final ChatHotelService chatHotelService;

    @PostMapping
    public ResponseEntity<String> traiterMessage(@RequestBody Map<String, String> body) {
        String message = body.get("message");
        try {
            String reponse = chatHotelService.repondre(message);
            return ResponseEntity.ok(reponse);
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.internalServerError().body("Erreur : " + e.getMessage());
        }
    }
}
