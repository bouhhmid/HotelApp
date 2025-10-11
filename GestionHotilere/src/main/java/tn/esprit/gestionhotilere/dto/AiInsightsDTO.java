package tn.esprit.gestionhotilere.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class AiInsightsDTO {
    private String insight; // message généré par Ollama
    private String type;    // ex: "forecast", "anomaly", "suggestion"
    private Date generatedAt;
}