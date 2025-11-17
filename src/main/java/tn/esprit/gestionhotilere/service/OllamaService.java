package tn.esprit.gestionhotilere.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;

@Service
public class OllamaService {
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    public JsonNode analyserMessage(String message) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String analysePrompt = """
Tu es un assistant d'hôtel intelligent. Ta tâche est de comprendre la phrase d'un utilisateur
et de retourner **uniquement un JSON valide** indiquant son intention.

Tu ne dois **jamais ajouter d'explication ou de texte**, seulement un JSON clair.

Les intentions possibles sont :
- salutation
- rechercher_hotel
- services_hotel
- prix_chambre
- reserver_chambre
- localisation_hotel
- horaires_checkin
- annuler_reservation
- contact_hotel
- remerciement

Exemples :

Phrase : "Bonjour"
Réponse : {"intention": "salutation"}


Phrase : "Merci pour votre aide"
Réponse : {"intention": "remerciement"}

Phrase : "Quels sont les hôtels disponibles à Sousse ?"
Réponse : {"intention": "rechercher_hotel", "ville": "Sousse"}

Phrase : "Je veux réserver une chambre à Monastir"
Réponse : {"intention": "reserver_chambre", "ville": "Monastir"}

Phrase : "Quels services proposez-vous à Tunis ?"
Réponse : {"intention": "services_hotel", "ville": "Tunis"}

Phrase : "Quels sont les prix des chambres à Hammamet ?"
Réponse : {"intention": "prix_chambre", "ville": "Hammamet"}

Phrase : "Où se trouve l'hôtel Sousse Palace ?"
Réponse : {"intention": "localisation_hotel", "nom": "Sousse Palace"}

Phrase : "Quels sont les horaires de check-in à Mahdia ?"
Réponse : {"intention": "horaires_checkin", "ville": "Mahdia"}

Phrase : "Je veux annuler ma réservation"
Réponse : {"intention": "annuler_reservation"}

Phrase : "Comment contacter l'hôtel Thalassa ?"
Réponse : {"intention": "contact_hotel", "nom": "Thalassa"}

Voici la phrase utilisateur :
"%s"
""".formatted(message.replace("\"", "\\\""));

        ObjectNode bodyNode = mapper.createObjectNode();
        bodyNode.put("model", "llama3");
        bodyNode.put("prompt", analysePrompt);
        bodyNode.put("stream", false);
        String body = mapper.writeValueAsString(bodyNode);

        System.out.println("🔹 Prompt envoyé :\n" + analysePrompt);
        System.out.println("🔸 JSON envoyé à Ollama :\n" + body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("🟢 Réponse brute d’Ollama :\n" + response.body());

        // 🔥 Étape essentielle : parser la réponse JSON
        JsonNode fullResponse = mapper.readTree(response.body());

        if (!fullResponse.has("response")) {
            System.err.println("❌ Le champ 'response' est manquant !");
            throw new IllegalArgumentException("Champ 'response' absent : " + response.body());
        }

        // 🧠 La vraie réponse est encodée dans 'response'
        String jsonString = fullResponse.get("response").asText(); // chaîne JSON dans une string

        // 🔁 Deuxième parsing du JSON
        JsonNode result = mapper.readTree(jsonString);

        System.out.println("🟣 JSON final analysé :\n" + result.toPrettyString());
        return result;
    }
    /** Génère une réponse texte (renvoie le champ 'response' d’Ollama). */
    public String generate(String prompt, String model) throws IOException, InterruptedException {
        ObjectNode bodyNode = mapper.createObjectNode();
        bodyNode.put("model", model != null ? model : "llama3");
        bodyNode.put("prompt", prompt);
        bodyNode.put("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bodyNode)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode full = mapper.readTree(response.body());
        return full.path("response").asText("");
    }


}
