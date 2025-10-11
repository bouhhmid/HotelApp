package tn.esprit.gestionhotilere.dto;

import java.util.Map;

public record ReviewAggregateDTO(
        Double averageScore,        // moyenne globale des avis
        Long count,                 // nombre total d’avis
        Map<String, Double> criteria // moyennes par critère (propreté, etc.)
) {}