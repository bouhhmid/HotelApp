//package tn.esprit.gestionhotilere.service;
//
//import com.stripe.Stripe;
//import com.stripe.exception.StripeException;
//import com.stripe.model.PaymentIntent;
//import com.stripe.model.checkout.Session;
//import com.stripe.param.PaymentIntentCreateParams;
//import com.stripe.param.checkout.SessionCreateParams;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import tn.esprit.gestionhotilere.entity.Paiement;
//import tn.esprit.gestionhotilere.entity.Reservation;
//import tn.esprit.gestionhotilere.entity.Statut;
//import tn.esprit.gestionhotilere.entity.StatutPaiement;
//import tn.esprit.gestionhotilere.exception.BusinessException;
//import tn.esprit.gestionhotilere.repository.PaiementRepository;
//import tn.esprit.gestionhotilere.repository.ReservationRepository;
//
//import java.math.BigDecimal;
//import java.util.Date;
//
//@Service
//@RequiredArgsConstructor
//public class PaiementService {
//
//    private final ReservationRepository reservationRepository;
//    private final PaiementRepository paiementRepository;
//    private final EmailService emailService;
//
//    @Value("${stripe.secret-key}")
//    private String stripeSecretKey;
//
//    public Paiement creerPaiementPourReservation(Long reservationId) {
//        Reservation reservation = reservationRepository.findById(reservationId)
//                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
//
//        Stripe.apiKey = stripeSecretKey;
//
//        double montant = calculerMontant(reservation);
//        long montantEnCents = (long) (montant * 100);
//
//        // ✅ Vérification : montant minimum Stripe (en centimes) = 50
//        if (montantEnCents < 50) {
//            throw new BusinessException(HttpStatus.BAD_REQUEST,
//                    "Le montant minimum autorisé pour un paiement Stripe est de 0.50 EUR");
//        }
//
//        try {
//            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                    .setAmount(montantEnCents)
//                    .setCurrency("eur")
//                    .setAutomaticPaymentMethods(
//                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
//                    )
//                    .build();
//
//            PaymentIntent intent = PaymentIntent.create(params);
//
//            Paiement paiement = new Paiement();
//            paiement.setMontant(montant);
//            paiement.setDevise("eur");
//            paiement.setStatut(StatutPaiement.PENDING);
//            paiement.setPaymentIntentId(intent.getId());
//            paiement.setDatePaiement(new Date());
//            paiement.setReservation(reservation);
//
//            reservation.setPaiement(paiement);
//            return paiementRepository.save(paiement);
//
//        } catch (StripeException e) {
//            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
//                    "Erreur Stripe : " + e.getMessage());
//        }
//    }
//
//    private BigDecimal calculerMontant(Reservation reservation) {
//        if (reservation.getChambre() != null) {
//            long nbJours = (reservation.getDateFin().getTime() - reservation.getDateDebut().getTime()) / (1000 * 60 * 60 * 24);
//            return reservation.getChambre()
//                    .getPrixBase()
//                    .multiply(BigDecimal.valueOf(Math.max(nbJours, 1))); // ✅ multiplication BigDecimal
//        } else if (reservation.getServiceHotel() != null) {
//            return BigDecimal.valueOf(reservation.getServiceHotel().getPrix());
//        } else {
//            throw new BusinessException(HttpStatus.BAD_REQUEST, "Montant impossible à calculer");
//        }
//    }
//
//
//    public String creerCheckoutSession(Long reservationId) throws StripeException {
//        Reservation reservation = reservationRepository.findById(reservationId)
//                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation introuvable"));
//
//        Stripe.apiKey = stripeSecretKey;
//
//        double montant = calculerMontant(reservation);
//        long montantEnCents = (long) (montant * 100);
//
//        SessionCreateParams params = SessionCreateParams.builder()
//                .setMode(SessionCreateParams.Mode.PAYMENT)
//                .setSuccessUrl("http://localhost:4200/success")
//                .setCancelUrl("http://localhost:4200/cancel")
//                .addLineItem(
//                        SessionCreateParams.LineItem.builder()
//                                .setQuantity(1L)
//                                .setPriceData(
//                                        SessionCreateParams.LineItem.PriceData.builder()
//                                                .setCurrency("eur")
//                                                .setUnitAmount(montantEnCents)
//                                                .setProductData(
//                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                                                                .setName("Réservation n°" + reservation.getId())
//                                                                .build())
//                                                .build())
//                                .build())
//                .build();
//
//        Session session = Session.create(params);
//        return session.getUrl();
//    }
//
//    public Reservation confirmerPaiement(String paymentIntentId) {
//        Paiement paiement = paiementRepository.findByPaymentIntentId(paymentIntentId)
//                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Paiement introuvable"));
//
//        paiement.setStatut(StatutPaiement.SUCCEEDED);
//        paiementRepository.save(paiement);
//
//        Reservation reservation = paiement.getReservation();
//
//
//        if (reservation != null && reservation.getClient() != null) {
//            String clientName = reservation.getClient().getNom();
//            String emailClient = reservation.getClient().getEmail();
//            String hotelName = reservation.getChambre() != null
//                    ? reservation.getChambre().getHotel().getNom()
//                    : reservation.getServiceHotel().getHotel().getNom();
//
//            String chambreLabel = reservation.getChambre() != null
//                    ? reservation.getChambre().getTypeChambre() + " #" + reservation.getChambre().getNumero()
//                    : reservation.getServiceHotel().getNomService();
//
//            String dateDebut = reservation.getDateDebut().toString();
//            String dateFin = reservation.getDateFin() != null ? reservation.getDateFin().toString() : "le même jour";
//
//            emailService.sendReservationConfirmation(
//                    emailClient,
//                    clientName,
//                    hotelName,
//                    chambreLabel,
//                    dateDebut,
//                    dateFin
//            );
//        }
//
//        return reservation;
//    }
//
//
//}
