package tn.esprit.gestionhotilere.dto;

import java.time.Instant;
import java.util.Map;

public record ReviewDTO(
        Long id,
        String userName,
        Double score,
        String title,
        String comment,
        Instant createdAt
) {}