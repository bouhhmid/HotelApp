package tn.esprit.gestionhotilere.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.gestionhotilere.entity.Notification;
import tn.esprit.gestionhotilere.entity.User;
import tn.esprit.gestionhotilere.service.NotificationService;
import tn.esprit.gestionhotilere.service.UserService;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /* === Récupérer les notifs de l’utilisateur connecté === */
    @GetMapping
    public ResponseEntity<List<Notification>> mesNotifications() {
        User me = userService.getOrCreateCurrentUser();
        return ResponseEntity.ok(notificationService.getNotificationsParUser(me));
    }

    /* === Version paginée === */
    @GetMapping("/page")
    public ResponseEntity<Page<Notification>> mesNotificationsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User me = userService.getOrCreateCurrentUser();
        return ResponseEntity.ok(notificationService.getNotificationsPage(me, page, size));
    }

    /* === Compter les notifs non lues === */
    @GetMapping("/unread")
    public ResponseEntity<Long> countUnread() {
        User me = userService.getOrCreateCurrentUser();
        return ResponseEntity.ok(notificationService.countUnread(me));
    }

    /* === Marquer comme lue === */
    @PutMapping("/{notifId}/lu")
    public ResponseEntity<Void> marquerCommeLue(@PathVariable Long notifId) {
        User me = userService.getOrCreateCurrentUser();
        notificationService.marquerCommeLue(notifId, me);
        return ResponseEntity.noContent().build();
    }

    /* === Récupérer les notifs en attente (ADMIN) === */
    @GetMapping("/admin/en-attente")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<List<Notification>> reservationsEnAttente() {
        User admin = userService.getOrCreateCurrentUser();
        return ResponseEntity.ok(notificationService.getNotificationsParUser(admin));
    }
}
