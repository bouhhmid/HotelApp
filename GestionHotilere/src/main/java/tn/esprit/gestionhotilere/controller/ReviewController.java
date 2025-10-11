package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import tn.esprit.gestionhotilere.dto.ReviewAggregateDTO;
import tn.esprit.gestionhotilere.dto.ReviewDTO;
import tn.esprit.gestionhotilere.dto.ReviewDTOIn;
import tn.esprit.gestionhotilere.service.ReviewService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hotels/{hotelId}/reviews")
@CrossOrigin(origins = "http://localhost:4200")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Liste paginée des avis d’un hôtel
     * GET /api/hotels/{hotelId}/reviews?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<ReviewDTO>> list(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewDTO> result = reviewService.list(hotelId, page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Agrégats (moyenne globale, nombre d’avis, moyennes par critère)
     * GET /api/hotels/{hotelId}/reviews/aggregates
     */
    @GetMapping("/aggregates")
    public ResponseEntity<ReviewAggregateDTO> aggregates(@PathVariable Long hotelId) {
        ReviewAggregateDTO agg = reviewService.aggregates(hotelId);
        return ResponseEntity.ok(agg);
    }


    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT','USER')") // adapte à tes rôles
    public ResponseEntity<ReviewDTO> add(
            @PathVariable Long hotelId,
            @Valid @RequestBody ReviewDTOIn in,
            Authentication authentication
    ) {
        // Récupération du username depuis le contexte de sécurité
        String userName = (authentication != null) ? authentication.getName() : "anonymous";
        ReviewDTO created = reviewService.add(hotelId, in, userName);
        return ResponseEntity.ok(created);
    }
}
