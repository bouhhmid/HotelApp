package tn.esprit.gestionhotilere.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.Hotel;
import tn.esprit.gestionhotilere.repository.HotelRepository;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatHotelService {

    private final HotelRepository hotelRepository;
    private final OllamaService ollamaService;

    public String repondre(String message) throws IOException, InterruptedException {
        JsonNode resultat = ollamaService.analyserMessage(message);

        String intention = resultat.has("intention") ? resultat.get("intention").asText() : null;
        String ville = resultat.has("ville") ? resultat.get("ville").asText() : null;

        // 🧠 Gestion des intentions intelligentes
        switch (intention != null ? intention : "") {
            case "salutation":
                return "Bonjour ! Comment puis-je vous aider à trouver un hôtel ou faire une réservation ?";

            case "rechercher_hotel":
                return traiterRechercheHotel(ville);

            case "services_hotel":
                return traiterServicesHotel(ville);

            case "prix_chambre":
                return "Pour vous donner les prix des chambres, merci de préciser la ville ou l'hôtel.";

            case "reserver_chambre":
                return "Très bien ! Pour réserver, merci de me donner la ville, la date et le nombre de personnes.";

            default:
                return "Je n'ai pas compris votre demande. Pouvez-vous reformuler ?";
        }
    }

    private String traiterRechercheHotel(String ville) {
        if (ville == null || ville.isBlank()) {
            return "Veuillez préciser la ville pour rechercher des hôtels.";
        }

        List<Hotel> hotels = hotelRepository.findByAdresseContainingIgnoreCase(ville);
        if (hotels.isEmpty()) {
            return "Aucun hôtel trouvé à " + ville + ".";
        }

        StringBuilder response = new StringBuilder("Voici les hôtels disponibles à " + ville + " :\n");
        for (Hotel hotel : hotels) {
            response.append("• ").append(hotel.getNom())
                    .append(" - ").append(hotel.getAdresse())
                    .append(hotel.getEtoiles() != null ? " (" + hotel.getEtoiles() + "⭐)" : "")
                    .append("\n");
        }
        return response.toString();
    }

    private String traiterServicesHotel(String ville) {
        if (ville == null || ville.isBlank()) {
            return "Merci de préciser la ville de l'hôtel pour consulter ses services.";
        }

        List<Hotel> hotels = hotelRepository.findByAdresseContainingIgnoreCase(ville);
        if (hotels.isEmpty()) {
            return "Aucun hôtel trouvé à " + ville + ".";
        }

        StringBuilder response = new StringBuilder("Voici les services proposés par les hôtels à " + ville + " :\n");
        for (Hotel hotel : hotels) {
            response.append("• ").append(hotel.getNom()).append(" :\n");
            if (hotel.getServiceHotels() == null || hotel.getServiceHotels().isEmpty()) {
                response.append("   Aucun service enregistré.\n");
            } else {
                hotel.getServiceHotels().forEach(service ->
                        response.append("   - ").append(service.getNomService()).append("\n"));
            }
        }
        return response.toString();
    }
}
