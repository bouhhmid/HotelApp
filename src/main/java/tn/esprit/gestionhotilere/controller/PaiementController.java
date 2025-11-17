//package tn.esprit.gestionhotilere.controller;
//
//import com.stripe.exception.SignatureVerificationException;
//import com.stripe.exception.StripeException;
//import com.stripe.model.Event;
//import com.stripe.model.checkout.Session;
//import com.stripe.net.Webhook;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
//import org.springframework.web.bind.annotation.*;
//import tn.esprit.gestionhotilere.entity.Paiement;
//import tn.esprit.gestionhotilere.entity.Reservation;
//import tn.esprit.gestionhotilere.service.PaiementService;
//import tn.esprit.gestionhotilere.service.ReservationService;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/paiement")
//@RequiredArgsConstructor
//public class PaiementController {
//    private final PaiementService paiementService;
//    private final ReservationService reservationService;
//
//    @Value("${stripe.secret-key}")
//    private String stripeSecretKey;
//
//    @Value("${stripe.webhook-secret}")
//    private String endpointSecret;
//
//
//    @PostMapping("/reservation/{id}")
//    public ResponseEntity<Paiement> payerReservation(@PathVariable("id") Long reservationId) {
//        Paiement paiement = paiementService.creerPaiementPourReservation(reservationId);
//        return ResponseEntity.ok(paiement);
//    }
//
//
//   // @PostMapping("/checkout/{reservationId}")
//    //@PreAuthorize("hasRole('ROLE_CLIENT')")
////    public ResponseEntity<Map<String, String>> checkout(
////            @PathVariable Long reservationId,
////            JwtAuthenticationToken authentication
////    ) {
////        String email = authentication.getToken().getClaimAsString("email"); // ou "preferred_username"
////      //  Reservation reservation = reservationService.findById(reservationId);
////
////        if (!reservation.getClient().getEmail().equalsIgnoreCase(email)) {
////            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Accès refusé"));
////        }
////
////        try {
////            String checkoutUrl = paiementService.creerCheckoutSession(reservationId);
////            return ResponseEntity.ok(Map.of("url", checkoutUrl));
////        } catch (StripeException e) {
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
////                    .body(Map.of("error", "Erreur Stripe : " + e.getMessage()));
////        }
////
//}
//  //  @PostMapping("/webhook")
////    public ResponseEntity<String> handleStripeWebhook(
////            @RequestBody String payload,
////            @RequestHeader("Stripe-Signature") String sigHeader) {
////        try {
////            System.out.println("🎯 Stripe Webhook reçu");
////            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
////
////            // 🔍 Type d'événement : checkout.session.completed
////            if ("checkout.session.completed".equals(event.getType())) {
////                System.out.println("✅ Événement : checkout.session.completed");
////
////                // 🧩 Désérialisation sécurisée de l'objet
////                var deserialized = event.getDataObjectDeserializer().getObject();
////                if (deserialized.isPresent()) {
////                    Object rawObject = deserialized.get();
////
////                    if (rawObject instanceof Session session) {
////                        String paymentIntentId = session.getPaymentIntent();
////                        System.out.println("🧾 PaymentIntent ID reçu : " + paymentIntentId);
////
////                        // ✅ Confirme le paiement et met à jour la réservation
////                        Reservation reservation = paiementService.confirmerPaiement(paymentIntentId);
////
////                        System.out.println("✅ Réservation confirmée : " + reservation.getId());
////
////                        return ResponseEntity.ok("Paiement confirmé pour réservation #" + reservation.getId());
////                    } else {
////                        System.out.println("❌ L'objet reçu n'est pas une session : " + rawObject.getClass().getSimpleName());
////                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Type inattendu : " + rawObject.getClass().getSimpleName());
////                    }
////                } else {
////                    System.out.println("❌ Échec de désérialisation : objet vide");
////                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Échec de désérialisation Stripe");
////                }
////            }
////
////            // ✅ Tous les autres événements sont ignorés
////            return ResponseEntity.ok("Événement ignoré : " + event.getType());
////
////        } catch (SignatureVerificationException e) {
////            System.out.println("❌ Signature Stripe invalide : " + e.getMessage());
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature invalide");
////        } catch (Exception e) {
////            System.out.println("❌ Erreur dans handleStripeWebhook : " + e.getMessage());
////            e.printStackTrace();
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur Webhook : " + e.getMessage());
////        }
////    }
//
////}