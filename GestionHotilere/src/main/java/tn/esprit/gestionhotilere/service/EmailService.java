package tn.esprit.gestionhotilere.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.entity.Reservation;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;


    public void sendReservationConfirmation(String toEmail, String clientName, String hotelName,
                                            String chambreLabel, String dateDebut, String dateFin) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, hotelName + " (Gestion Hôtelière)");
            helper.setTo(toEmail);
            helper.setSubject("Confirmation de votre réservation - " + hotelName);

            String body = buildEmailBody(clientName, hotelName, chambreLabel, dateDebut, dateFin);
            helper.setText(body, true); // HTML

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new IllegalStateException("Erreur lors de l'envoi de l'email", e);
        }
    }

    private String buildEmailBody(String clientName, String hotelName, String chambreLabel,
                                  String dateDebut, String dateFin) {
        return """
            <div style="font-family: Arial, sans-serif; padding: 20px;">
              <h2>Bonjour %s,</h2>
              <p>Merci pour votre réservation à l'hôtel <strong>%s</strong>.</p>
              <p><strong>Détails de la réservation :</strong></p>
              <ul>
                <li>Chambre : %s</li>
                <li>Date d'arrivée : %s</li>
                <li>Date de départ : %s</li>
              </ul>
              <p>Nous avons hâte de vous accueillir !</p>
              <br>
              <p style="font-size: 12px; color: gray;">Ceci est un email automatique. Merci de ne pas répondre.</p>
            </div>
            """.formatted(clientName, hotelName, chambreLabel, dateDebut, dateFin);
    }
    public void envoyerEmailConfirmationPaiement(Reservation reservation) {
        String email = reservation.getClient().getEmail();
        String clientName = reservation.getClient().getNom();
        String hotelName = reservation.getChambre() != null ?
                reservation.getChambre().getHotel().getNom() :
                reservation.getServiceHotel().getHotel().getNom();
        String label = reservation.getChambre() != null ?
                "Chambre " + reservation.getChambre().getNumero() :
                "Service " + reservation.getServiceHotel().getNomService();
        String dateDebut = reservation.getDateDebut().toString();
        String dateFin = reservation.getDateFin().toString();

        sendReservationConfirmation(email, clientName, hotelName, label, dateDebut, dateFin);
    }

}
