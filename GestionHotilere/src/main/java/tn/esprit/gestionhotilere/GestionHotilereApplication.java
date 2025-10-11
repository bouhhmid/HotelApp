package tn.esprit.gestionhotilere;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class GestionHotilereApplication {

  public static void main(String[] args) {

    // 🟢 Charger manuellement le fichier .env avant Spring Boot
    Dotenv dotenv = Dotenv.configure()
      .ignoreIfMissing() // ignore si .env manquant
      .load();

    // Injecter les variables dans les System Properties
    dotenv.entries().forEach(entry ->
      System.setProperty(entry.getKey(), entry.getValue())
    );

    // Lancer l'application Spring Boot
    SpringApplication.run(GestionHotilereApplication.class, args);
  }

  // 🧩 Vérification au démarrage
  @PostConstruct
  public void testEnv() {
    System.out.println("🔹 KEYCLOAK_ISSUER_URI = " + System.getenv("KEYCLOAK_ISSUER_URI"));
    System.out.println("🔹 DB_URL = " + System.getenv("DB_URL"));
    System.out.println("🔹 STRIPE_SECRET_KEY = " + System.getenv("STRIPE_SECRET_KEY"));
  }
}
