// tn/esprit/gestionhotilere/service/AiInsightsService.java
package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.AiInsightsDTO;
import tn.esprit.gestionhotilere.dto.AdminOverviewDTO;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInsightsService {

    private final StatsService statsService;
    private final OllamaService ollama;

    public AiInsightsDTO buildInsights(Long hotelId, int windowDays, ZoneId tz) {
        var opts = new StatsService.OverviewOptions(windowDays, false, 1, tz.toString(), true, false);
        AdminOverviewDTO ov = statsService.buildAdminOverview(hotelId, opts);

        String seriesStr = toSeriesString(ov);
        String prompt = """
            Tu es un copilote hôtelier. Analyse les données et réponds en FR en respectant ce format exact:

            ### SUMMARY
            (2 phrases maximum)

            ### ACTIONS
            - (3 à 5 actions, chaque ligne commence par un verbe, <=16 mots)

            ### RISKS
            - (0 à 3 risques sur 48h, sinon rien)

            Données:
            OCCUP_TODAY: %.1f
            PENDING: %d
            CHECKINS_TODAY: %d
            SERIES_LAST_%d: %s
            """.formatted(
                safeD(ov.getTauxOccupationJour()),
                safeL(ov.getNbReservationsEnAttente()),
                safeL(ov.getNbReservationsJour()),
                windowDays,
                seriesStr
        );

        try {
            String answer = ollama.generate(prompt, "llama3");
            return parseMarkdownToSingle(answer);
        } catch (Exception e) {
            return fallbackFrom(ov);
        }
    }

    private static String toSeriesString(AdminOverviewDTO ov){
        if (ov == null || ov.getReservations7J() == null) return "[]";
        var sb = new StringBuilder("[");
        for (var p : ov.getReservations7J()) {
            sb.append("(").append(p.getX()).append(", ").append(p.getY()).append("), ");
        }
        if (sb.length() > 1) sb.setLength(sb.length()-2);
        sb.append("]");
        return sb.toString();
    }

    /** On ne garde que la section SUMMARY -> dans AiInsightsDTO.insight */
    private static AiInsightsDTO parseMarkdownToSingle(String md){
        String summary = between(md, "### SUMMARY", "### ACTIONS").trim();
        return AiInsightsDTO.builder()
                .insight(summary.isBlank() ? "Analyse indisponible." : summary)
                .type("insight")
                .generatedAt(new Date())
                .build();
    }

    private static String between(String s, String a, String b){
        int i = s.indexOf(a); if (i < 0) return "";
        int j = (b == null) ? s.length() : s.indexOf(b, i);
        if (j < 0) j = s.length();
        return s.substring(i + a.length(), j).trim();
    }

    private static double safeD(Double d){ return d == null ? 0 : d; }
    private static long safeL(Long l){ return l == null ? 0L : l; }

    private static AiInsightsDTO fallbackFrom(AdminOverviewDTO ov){
        var vals = new ArrayList<Double>();
        if (ov != null && ov.getReservations7J() != null){
            for (var p: ov.getReservations7J()) vals.add(p.getY());
        }
        double avg = vals.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double last = vals.isEmpty() ? 0 : vals.get(vals.size()-1);
        String dir = last > avg*1.1 ? "hausse" : last < avg*0.9 ? "baisse" : "stabilité";

        String summary = "Demande en " + dir + " (moyenne " + String.format("%.1f", avg) +
                ", dernier " + (int)last + ").";

        return AiInsightsDTO.builder()
                .insight(summary)
                .type("fallback")
                .generatedAt(new Date())
                .build();
    }
}
