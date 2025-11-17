package tn.esprit.gestionhotilere.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import tn.esprit.gestionhotilere.entity.EmailTemplate;
import tn.esprit.gestionhotilere.entity.Notification;
import tn.esprit.gestionhotilere.entity.NotificationType;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.repository.EmailTemplateRepository;
import tn.esprit.gestionhotilere.repository.NotificationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final JavaMailSender mailSender;
    private final SimpMessagingTemplate ws;
    private final TemplateEngine thymeleaf; // <- sera le stringTemplateEngine

    @Value("${app.mail.from:no-reply@gestionhoteliere.local}")
    private String mailFrom;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            EmailTemplateRepository emailTemplateRepository,
            JavaMailSender mailSender,
            SimpMessagingTemplate ws,
            @Qualifier("stringTemplateEngine") TemplateEngine thymeleaf
    ) {
        this.notificationRepository = notificationRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.mailSender = mailSender;
        this.ws = ws;
        this.thymeleaf = thymeleaf;
    }
    @Transactional
    public void envoyerNotificationEmail(User user, Long hotelId, String templateType, Map<String, Object> model) {
        // 0) Garde-fous
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
            notificationRepository.save(Notification.builder()
                    .titre(String.valueOf(model.getOrDefault("subject", "Notification")))
                    .message("Notification non envoyée par email (adresse manquante).")
                    .utilisateur(user)
                    .type(NotificationType.EMAIL)
                    .dateEnvoi(Instant.now())
                    .lu(false)
                    .build());
            return;
        }

        // 1) Récupération template (par hôtel puis global)
        EmailTemplate template = (hotelId != null)
                ? emailTemplateRepository.findByTypeEmailAndHotelId(templateType, hotelId)
                .or(() -> emailTemplateRepository.findByTypeEmailAndHotelIdIsNull(templateType))
                .orElse(null)
                : emailTemplateRepository.findByTypeEmailAndHotelIdIsNull(templateType)
                .orElse(null);

        // 2) Contexte + sujet
        String subject = String.valueOf(model.getOrDefault("subject", "Notification"));
        Context context = new Context();
        context.setVariables(model);
        context.setVariable("subject", subject); // utile si le template l'affiche

        // 3) Rendu HTML (BDD ou fallback)
        String html;
        if (template != null && template.getContenu() != null && !template.getContenu().isBlank()) {
            // Avec StringTemplateResolver, on passe le CONTENU directement à process()
            html = thymeleaf.process(template.getContenu(), context);
        } else {
            // Fallback minimal pour tester immédiatement l’envoi
            String defaultContent = """
            <html>
              <body>
                <h2 th:text="${subject}">Notification</h2>
                <p>Bonjour <span th:text="${userName}">Client</span>,</p>
                <p th:if="${templateType} == 'reservation-confirmed'">
                    Votre réservation à <b th:text="${hotelName}">Hôtel</b> est confirmée.
                </p>
                <p th:if="${templateType} == 'reservation-cancelled'">
                    Votre réservation à <b th:text="${hotelName}">Hôtel</b> a été annulée.
                </p>
                <p th:if="${periode != null}">Période : <span th:text="${periode}">du ... au ...</span></p>
                <p>Référence : <span th:text="${reservationId}">#id</span></p>
              </body>
            </html>
            """;
            context.setVariable("templateType", templateType);
            html = thymeleaf.process(defaultContent, context);
        }

        // 4) Envoi email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(mailFrom);                  // ex: spring.mail.username
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email", e);
        }

        // 5) Historiser
        notificationRepository.save(Notification.builder()
                .titre(subject)
                .message("Un email vous a été envoyé concernant votre réservation.")
                .utilisateur(user)
                .type(NotificationType.EMAIL)
                .dateEnvoi(Instant.now())
                .lu(false)
                .build());
    }



    /* ===================== WS ADMIN (création réservation) ===================== */
    @Transactional
    public Notification pushAdminReservationNotif(User admin,
                                                  Long hotelId,
                                                  Long reservationId,
                                                  String titre,
                                                  String message) {
        Notification n = Notification.builder()
                .titre(titre)
                .message(message)
                .type(NotificationType.WEBSOCKET) // ton enum : canal = WEBSOCKET
                .hotelId(hotelId)
                .reservationId(reservationId)
                .utilisateur(admin)
                .dateEnvoi(Instant.now())
                .lu(false)
                .build();
        n = notificationRepository.save(n);

        // Topic front: /topic/admin/{adminId}/notifications
        ws.convertAndSend("/topic/admin/" + admin.getId() + "/notifications", n);
        return n;
    }

    /* ===================== LIST / PAGE / BADGE ===================== */
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsParUser(User user) {
        return notificationRepository.findByUtilisateurIdOrderByDateEnvoiDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public Page<Notification> getNotificationsPage(User user, int page, int size) {
        return notificationRepository.findByUtilisateur_IdOrderByDateEnvoiDesc(
                user.getId(), PageRequest.of(Math.max(0, page), Math.max(1, size),
                        Sort.by(Sort.Direction.DESC, "dateEnvoi")));
    }

    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return notificationRepository.countByUtilisateur_IdAndLuFalse(user.getId());
    }

    @Transactional
    public void marquerCommeLue(Long notifId, User me) {
        Notification n = notificationRepository.findById(notifId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));
        if (!n.getUtilisateur().getId().equals(me.getId())) {
            throw new RuntimeException("Accès refusé");
        }
        n.setLu(true);
        notificationRepository.save(n);
    }
    @Transactional
    public Notification pushClientReservationNotif(User client,
                                                   Long hotelId,
                                                   Long reservationId,
                                                   String titre,
                                                   String message) {
        Notification n = Notification.builder()
                .titre(titre)
                .message(message)
                .type(NotificationType.WEBSOCKET)
                .hotelId(hotelId)
                .reservationId(reservationId)
                .utilisateur(client)
                .dateEnvoi(Instant.now())
                .lu(false)
                .build();
        n = notificationRepository.save(n);
        ws.convertAndSend("/topic/user/" + client.getId() + "/notifications", n);
        return n;
    }
}
