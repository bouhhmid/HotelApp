package tn.esprit.gestionhotilere.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.*;
import tn.esprit.gestionhotilere.entity.*;
import tn.esprit.gestionhotilere.exception.BusinessException;
import tn.esprit.gestionhotilere.repository.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ServiceHotelRepository serviceHotelRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final ChambreService chambreService;
    private final ChambreOptionRepository chambreOptionRepository;
    private final NotificationService notificationService;

    /* ====================== CHAMBRE ====================== */

    public Reservation reserverChambre(Long chambreId, Date dateDebut, Date dateFin) {
        User client = userService.getOrCreateCurrentUser();

        if (dateDebut == null || dateFin == null || !dateDebut.before(dateFin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La date de début doit être avant la date de fin.");
        }
        if (reservationRepository.countOverlapsChambre(chambreId, dateDebut, dateFin) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Chambre déjà réservée pendant cette période.");
        }

        Chambre chambre = chambreService.getChambreEntityById(chambreId);
        Hotel hotel = chambre.getHotel();

        Reservation r = new Reservation();
        r.setClient(client);
        r.setChambre(chambre);
        r.setHotel(hotel);
        r.setDateDebut(dateDebut);
        r.setDateFin(dateFin);
        r.setDateReservation(new Date());
        r.setStatut(Statut.EN_ATTENTE);

        Reservation saved = reservationRepository.save(r);

        User admin = (hotel != null ? hotel.getAdmnistrateur() : null);
        if (admin != null) {
            notificationService.pushAdminReservationNotif(
                    admin,
                    hotel.getId(),
                    saved.getId(),
                    "Nouvelle réservation en attente",
                    client.getPrenom() + " a réservé la chambre " + chambre.getNumero() +
                            " de l’hôtel " + hotel.getNom()
            );
        }

        var fmt = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> model = new HashMap<>();
        model.put("subject", "Demande de réservation reçue");
        model.put("userName", client.getPrenom());
        model.put("hotelName", hotel.getNom());
        model.put("chambreNumero", chambre.getNumero());
        model.put("reservationId", saved.getId());
        model.put("periode", " du " + fmt.format(dateDebut) + " au " + fmt.format(dateFin));

        notificationService.envoyerNotificationEmail(
                client, hotel.getId(), "reservation-pending", model
        );


        return saved;
    }

    public ReservationChambreQuoteDTO quoteChambre(ReservationChambreRequestDTO dto) {
        Chambre chambre = chambreService.getChambreEntityById(dto.getChambreId());

        LocalDate d1 = dto.getDateDebut().toLocalDate();
        LocalDate d2 = dto.getDateFin().toLocalDate();
        long nuits = ChronoUnit.DAYS.between(d1, d2);
        if (nuits <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "Dates invalides (au moins 1 nuit).");

        int capParChambre = (chambre.getCapaciteAdulte() == null ? 0 : chambre.getCapaciteAdulte())
                + (chambre.getCapaciteEnfant() == null ? 0 : chambre.getCapaciteEnfant());
        int capMax = dto.getNbChambres() * capParChambre;
        int req = dto.getNbAdultes() + dto.getNbEnfants();
        if (req > capMax) {
            throw new BusinessException(HttpStatus.CONFLICT, "Capacité dépassée : requis " + req + " > max " + capMax);
        }

        Date start = Date.from(dto.getDateDebut().atZone(ZoneId.systemDefault()).toInstant());
        Date end   = Date.from(dto.getDateFin().atZone(ZoneId.systemDefault()).toInstant());
        if (reservationRepository.countOverlapsChambre(dto.getChambreId(), start, end) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Chambre déjà réservée pendant cette période.");
        }

        BigDecimal unit = chambre.getPrixBase();
        BigDecimal subtotal = unit
                .multiply(BigDecimal.valueOf(nuits))
                .multiply(BigDecimal.valueOf(dto.getNbChambres()));

        ReservationChambreQuoteDTO q = new ReservationChambreQuoteDTO();
        q.setNuits(nuits);
        q.setCapaciteRequise(req);
        q.setCapaciteMax(capMax);
        q.setCapacityOk(true);
        q.setSubtotal(subtotal);
        q.setTaxes(BigDecimal.ZERO);
        q.setFrais(BigDecimal.ZERO);
        q.setTotal(subtotal);
        q.setCurrency("TND");
        return q;
    }

    public ReservationChambreResponseDTO createChambreReservation(ReservationChambreRequestDTO dto) {
        ReservationChambreQuoteDTO quote = quoteChambre(dto);

        User client = userService.getOrCreateCurrentUser();
        Chambre chambre = chambreService.getChambreEntityById(dto.getChambreId());
        Hotel hotel = chambre.getHotel();

        Reservation r = new Reservation();
        r.setClient(client);
        r.setChambre(chambre);
        r.setHotel(hotel);
        r.setDateDebut(Date.from(dto.getDateDebut().atZone(ZoneId.systemDefault()).toInstant()));
        r.setDateFin(Date.from(dto.getDateFin().atZone(ZoneId.systemDefault()).toInstant()));
        r.setDateReservation(new Date());
        r.setStatut(Statut.EN_ATTENTE);
        r.setNbAdultes(dto.getNbAdultes());
        r.setNbEnfants(dto.getNbEnfants());
        r.setNbChambres(dto.getNbChambres());
        r.setTotalAmount(quote.getTotal());
        r.setCurrency(quote.getCurrency());

        Reservation saved = reservationRepository.save(r);

        User admin = (hotel != null ? hotel.getAdmnistrateur() : null);
        if (admin != null) {
            notificationService.pushAdminReservationNotif(
                    admin,
                    hotel.getId(),
                    saved.getId(),
                    "Nouvelle réservation en attente",
                    client.getPrenom() + " a réservé " + dto.getNbChambres() + " chambre(s) (" +
                            chambre.getNumero() + ") à l’hôtel " + hotel.getNom()
            );
        }

        var fmt = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> model = new HashMap<>();
        model.put("subject", "Demande de réservation reçue");
        model.put("userName", client.getPrenom());                   // ou prenom + nom
        model.put("hotelName", hotel.getNom());
        model.put("chambreNumero", chambre.getNumero());
        model.put("reservationId", saved.getId());
        model.put("periode", " du " + fmt.format(r.getDateDebut()) + " au " + fmt.format(r.getDateFin()));

        notificationService.envoyerNotificationEmail(
                client, hotel.getId(), "reservation-pending", model
        );


        ReservationChambreResponseDTO resp = new ReservationChambreResponseDTO();
        resp.setId(saved.getId());
        resp.setDateReservation(saved.getDateReservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setDateDebut(saved.getDateDebut().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setDateFin(saved.getDateFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setStatut(saved.getStatut());
        resp.setChambreId(chambre.getId());
        resp.setNbAdultes(dto.getNbAdultes());
        resp.setNbEnfants(dto.getNbEnfants());
        resp.setNbChambres(dto.getNbChambres());
        resp.setSubtotal(quote.getSubtotal());
        resp.setTaxes(quote.getTaxes());
        resp.setFrais(quote.getFrais());
        resp.setTotal(quote.getTotal());
        resp.setCurrency(quote.getCurrency());
        return resp;
    }

    /* ====================== SERVICE HÔTEL ====================== */

    public Reservation reserverService(Long serviceId, java.time.LocalDateTime dateDebut, java.time.LocalDateTime dateFin) {
        if (dateDebut == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "La date de début est obligatoire.");
        if (dateFin == null) dateFin = dateDebut.plusHours(1);
        if (!dateFin.isAfter(dateDebut)) throw new BusinessException(HttpStatus.BAD_REQUEST, "La date de fin doit être après la date de début.");

        ServiceHotel svc = serviceHotelRepository.findById(serviceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Service introuvable"));

        Date start = Date.from(dateDebut.atZone(ZoneId.systemDefault()).toInstant());
        Date end   = Date.from(dateFin.atZone(ZoneId.systemDefault()).toInstant());

        if (reservationRepository.countOverlapsService(serviceId, start, end) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ce service est déjà réservé pendant cet intervalle.");
        }

        Reservation r = new Reservation();
        r.setClient(userService.getOrCreateCurrentUser());
        r.setServiceHotel(svc);
        r.setHotel(svc.getHotel());
        r.setDateReservation(new Date());
        r.setDateDebut(start);
        r.setDateFin(end);
        r.setDateTime(start);
        r.setStatut(Statut.EN_ATTENTE);

        return reservationRepository.save(r);
    }

    @Transactional
    public Reservation reserverServiceV2(ReservationServiceRequestDTO dto) {
        if (dto.participants() < 1) throw new BusinessException(HttpStatus.BAD_REQUEST, "participants doit être >= 1");

        var svc = serviceHotelRepository.findById(dto.serviceHotelId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Service introuvable"));

        Date start = Date.from(dto.dateTime().atZone(ZoneId.systemDefault()).toInstant());
        Date end   = Date.from(dto.dateTime().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

        if (reservationRepository.countOverlapsService(svc.getId(), start, end) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Créneau déjà réservé");
        }

        BigDecimal total = BigDecimal.valueOf(svc.getPrix()).multiply(BigDecimal.valueOf(dto.participants()));

        var r = new Reservation();
        r.setClient(userService.getOrCreateCurrentUser());
        r.setServiceHotel(svc);
        r.setHotel(svc.getHotel());
        r.setDateReservation(new Date());
        r.setDateDebut(start);
        r.setDateFin(end);
        r.setDateTime(start);
        r.setParticipants(dto.participants());
        r.setStatut(Statut.EN_ATTENTE);
        r.setTotalAmount(total);
        r.setCurrency("TND");

        var saved = reservationRepository.save(r);

        Hotel hotel = svc.getHotel();
        User admin = (hotel != null ? hotel.getAdmnistrateur() : null);
        if (admin != null) {
            notificationService.pushAdminReservationNotif(
                    admin,
                    hotel.getId(),
                    saved.getId(),
                    "Nouvelle réservation service en attente",
                    r.getClient().getPrenom() + " a réservé le service " +
                            (svc.getNomService() != null ? svc.getNomService() : ("#" + svc.getId())) +
                            " pour " + dto.participants() + " personnes."
            );
        }
        return saved;
    }

    /* ====================== ACTIONS ====================== */

    public void annulerReservation(Long reservationId) {
        var me = userService.getOrCreateCurrentUser();
        var r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation non trouvée"));

        switch (me.getRole()) {
            case CLIENT -> {
                boolean owns = reservationRepository.clientOwnsReservation(reservationId, me.getId());
                if (!owns) throw new BusinessException(HttpStatus.FORBIDDEN, "Vous ne pouvez annuler que vos réservations.");
            }
            case ADMIN -> {
                boolean owns = reservationRepository.adminOwnsReservation(reservationId, me.getId());
                if (!owns) throw new BusinessException(HttpStatus.FORBIDDEN, "Accès refusé (hôtel non géré).");
            }
            case SUPERADMIN -> { /* ok */ }
            default -> throw new BusinessException(HttpStatus.FORBIDDEN, "Rôle non autorisé");
        }

        if (r.getStatut() == Statut.ANNULEE) return;

        r.setStatut(Statut.ANNULEE);
        r = reservationRepository.save(r);

        Long hotelId; String hotelName;
        if (r.getChambre() != null) {
            hotelId = r.getChambre().getHotel().getId();
            hotelName = r.getChambre().getHotel().getNom();
        } else {
            hotelId = r.getServiceHotel().getHotel().getId();
            hotelName = r.getServiceHotel().getHotel().getNom();
        }

        if (me.getRole() == Role.CLIENT) {
            User admin = (r.getChambre()!=null)
                    ? r.getChambre().getHotel().getAdmnistrateur()
                    : r.getServiceHotel().getHotel().getAdmnistrateur();
            if (admin != null) {
                notificationService.pushAdminReservationNotif(
                        admin, hotelId, r.getId(),
                        "Annulation par le client",
                        me.getPrenom() + " a annulé une réservation chez " + hotelName + "."
                );
            }
        } else {
            notificationService.pushClientReservationNotif(
                    r.getClient(), hotelId, r.getId(),
                    "Votre réservation a été annulée",
                    "Votre réservation chez " + hotelName + " a été annulée."
            );
            try {
                Map<String, Object> model = Map.of(
                        "userName", (r.getClient().getNom()==null?"":r.getClient().getNom()) + " " +
                                (r.getClient().getPrenom()==null?"":r.getClient().getPrenom()),
                        "hotelName", hotelName,
                        "subject", "Annulation de réservation"
                );
                notificationService.envoyerNotificationEmail(r.getClient(), hotelId, "reservation-cancelled", model);

            } catch (Exception ignore) {}
        }
    }

    public Reservation confirmerReservation(Long reservationId) {
        var me = userService.getOrCreateCurrentUser();
        if (me.getRole() != Role.ADMIN && me.getRole() != Role.SUPERADMIN) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Accès refusé");
        }

        var r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation non trouvée"));

        if (me.getRole() == Role.ADMIN) {
            boolean owns = reservationRepository.adminOwnsReservation(reservationId, me.getId());
            if (!owns) throw new BusinessException(HttpStatus.FORBIDDEN, "Accès refusé (hôtel non géré).");
        }

        if (r.getStatut() != Statut.EN_ATTENTE) {
            throw new BusinessException(HttpStatus.CONFLICT, "Seules les réservations EN_ATTENTE peuvent être confirmées.");
        }

        r.setStatut(Statut.CONFIRMEE);
        r = reservationRepository.save(r);

        Long hotelId; String hotelName;
        if (r.getChambre() != null) {
            hotelId = r.getChambre().getHotel().getId();
            hotelName = r.getChambre().getHotel().getNom();
        } else {
            hotelId = r.getServiceHotel().getHotel().getId();
            hotelName = r.getServiceHotel().getHotel().getNom();
        }

        var fmt = new SimpleDateFormat("dd/MM/yyyy");
        String periode = r.getDateDebut() != null
                ? " du " + fmt.format(r.getDateDebut()) + " au " + fmt.format(r.getDateFin())
                : "";

        Map<String, Object> model = new HashMap<>();
        model.put("subject", "Confirmation de réservation");
        model.put("userName", (r.getClient().getPrenom()==null?"":r.getClient().getPrenom())
                + " " + (r.getClient().getNom()==null?"":r.getClient().getNom()));
        model.put("hotelName", hotelName);
        if (r.getChambre() != null) model.put("chambreNumero", r.getChambre().getNumero());
        model.put("reservationId", r.getId());
        model.put("periode", periode);

        notificationService.envoyerNotificationEmail(
                r.getClient(), hotelId, "reservation-confirmed", model
        );

        return r;
    }

    /* ====================== LISTES ====================== */

    public Page<Reservation> mesReservations(int page, int size, Statut statut) {
        Long clientId = userService.getOrCreateCurrentUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        if (statut != null) {
            return reservationRepository.findByClientIdAndStatut(clientId, statut, pageable);
        }
        return reservationRepository.findByClientId(clientId, pageable);
    }

    public Page<Reservation> listerEnAttente(int page, int size) {
        User me = userService.getOrCreateCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        if (me.getRole() == Role.SUPERADMIN) {
            return reservationRepository.findByStatut(Statut.EN_ATTENTE, pageable);
        }
        if (me.getRole() == Role.ADMIN) {
            return reservationRepository.findByHotelAdminIdAndStatut(me.getId(), Statut.EN_ATTENTE, pageable);
        }
        throw new BusinessException(HttpStatus.FORBIDDEN, "Accès interdit");
    }

    public ReservationServiceQuoteDTO quoteService(ReservationServiceRequestDTO dto) {
        double prixUnitaire = 50.0; // à remplacer si pricing réel
        BigDecimal subtotal = BigDecimal.valueOf(dto.participants() * prixUnitaire);
        BigDecimal taxes = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal frais = BigDecimal.valueOf(5.0);
        BigDecimal total = subtotal.add(taxes).add(frais);

        return new ReservationServiceQuoteDTO(subtotal, taxes, frais, total, "TND", true);
    }

    public List<LocalDate> getDatesDisponibles(Long chambreId) {
        List<Object[]> reservations = reservationRepository.findReservationsPeriods(chambreId);
        Set<LocalDate> reservedDates = new HashSet<>();

        for (Object[] period : reservations) {
            Date start = (Date) period[0];
            Date end = (Date) period[1];

            LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LocalDate current = startDate;
            while (!current.isAfter(endDate.minusDays(1))) {
                reservedDates.add(current);
                current = current.plusDays(1);
            }
        }

        LocalDate today = LocalDate.now();
        LocalDate endPeriod = today.plusMonths(3);

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = today;

        while (!current.isAfter(endPeriod)) {
            if (!reservedDates.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        return availableDates;
    }

    public List<ChambreOptionDTO> listOptionsByChambre(Long chambreId) {
        return chambreOptionRepository.findByChambreIdAndActifTrue(chambreId)
                .stream()
                .map(opt -> ChambreOptionDTO.builder()
                        .id(opt.getId())
                        .chambreId(opt.getChambre().getId())
                        .typeChambre(opt.getTypeChambre())
                        .vue(opt.getVue())
                        .prixParNuitParPersonne(opt.getPrixParNuitParPersonne())
                        .build())
                .toList();
    }

    public ReservationChambreQuoteDTO quoteChambreOption(ReservationChambreOptionRequestDTO dto) {
        var opt = chambreOptionRepository.findById(dto.getOptionId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Option de chambre introuvable"));

        long nuits = ChronoUnit.DAYS.between(dto.getDateDebut(), dto.getDateFin());
        if (nuits <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "Dates invalides (>= 1 nuit)");

        var ch = opt.getChambre();
        int capParChambre = (ch.getCapaciteAdulte()==null?0:ch.getCapaciteAdulte()) +
                (ch.getCapaciteEnfant()==null?0:ch.getCapaciteEnfant());
        if (capParChambre <= 0) throw new BusinessException(HttpStatus.BAD_REQUEST, "Capacité non configurée");

        int req = dto.getNbAdultes() + dto.getNbEnfants();
        int capMax = dto.getNbChambres() * capParChambre;
        if (req > capMax) throw new BusinessException(HttpStatus.CONFLICT, "Capacité dépassée");

        Date start = Date.from(dto.getDateDebut().atZone(ZoneId.systemDefault()).toInstant());
        Date end   = Date.from(dto.getDateFin().atZone(ZoneId.systemDefault()).toInstant());
        if (reservationRepository.countOverlapsChambre(ch.getId(), start, end) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "Chambre déjà réservée sur ces dates");
        }

        int personnes = req;
        BigDecimal unit = opt.getPrixParNuitParPersonne();
        BigDecimal subtotal = unit
                .multiply(BigDecimal.valueOf(personnes))
                .multiply(BigDecimal.valueOf(nuits))
                .multiply(BigDecimal.valueOf(dto.getNbChambres()));

        var q = new ReservationChambreQuoteDTO();
        q.setNuits(nuits);
        q.setCapaciteRequise(req);
        q.setCapaciteMax(capMax);
        q.setCapacityOk(true);
        q.setSubtotal(subtotal);
        q.setTaxes(BigDecimal.ZERO);
        q.setFrais(BigDecimal.ZERO);
        q.setTotal(subtotal);
        q.setCurrency("TND");
        return q;
    }

    public ReservationChambreResponseDTO createChambreReservationOption(ReservationChambreOptionRequestDTO dto) {
        var quote = quoteChambreOption(dto);
        var opt = chambreOptionRepository.findById(dto.getOptionId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Option de chambre introuvable"));

        User client = userService.getOrCreateCurrentUser();
        var ch = opt.getChambre();
        var hotel = ch.getHotel();

        var r = new Reservation();
        r.setClient(client);
        r.setChambre(ch);
        r.setHotel(hotel);                                         // ✅ cohérent avec le repo
        // si tu as un lien Reservation -> ChambreOption, tu peux aussi faire : r.setChambreOption(opt);

        r.setDateDebut(Date.from(dto.getDateDebut().atZone(ZoneId.systemDefault()).toInstant()));
        r.setDateFin(  Date.from(dto.getDateFin().atZone(ZoneId.systemDefault()).toInstant()));
        r.setDateReservation(new Date());
        r.setStatut(Statut.EN_ATTENTE);
        r.setNbAdultes(dto.getNbAdultes());
        r.setNbEnfants(dto.getNbEnfants());
        r.setNbChambres(dto.getNbChambres());
        r.setTotalAmount(quote.getTotal());
        r.setCurrency(quote.getCurrency());

        var saved = reservationRepository.save(r);

        User admin = (hotel != null ? hotel.getAdmnistrateur() : null);
        if (admin != null) {
            String titre = "Nouvelle réservation d’option de chambre";
            String message = client.getPrenom()
                    + " a réservé " + dto.getNbChambres() + " chambre(s) – option "
                    + (opt.getTypeChambre() != null ? opt.getTypeChambre().name() : "N/A")
                    + (opt.getVue() != null ? (" / " + opt.getVue()) : "")
                    + " (chambre " + ch.getNumero() + ") à l’hôtel " + hotel.getNom()
                    + " du " + new SimpleDateFormat("yyyy-MM-dd").format(r.getDateDebut())
                    + " au " + new SimpleDateFormat("yyyy-MM-dd").format(r.getDateFin()) + ".";

            notificationService.pushAdminReservationNotif(
                    admin,
                    hotel.getId(),
                    saved.getId(),
                    titre,
                    message
            );
        }

        var fmt = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Object> model = new HashMap<>();
        model.put("subject", "Demande de réservation reçue");
        model.put("userName", client.getPrenom()); // ou prenom + nom
        model.put("hotelName", hotel.getNom());
        model.put("chambreNumero", ch.getNumero()); // <- juste le numéro de chambre
        model.put("reservationId", saved.getId());
        model.put("periode", "du " + fmt.format(r.getDateDebut()) + " au " + fmt.format(r.getDateFin()));


        notificationService.envoyerNotificationEmail(
                client, hotel.getId(), "reservation-pending", model);

        var resp = new ReservationChambreResponseDTO();
        resp.setId(saved.getId());
        resp.setDateReservation(saved.getDateReservation().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setDateDebut(saved.getDateDebut().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setDateFin(saved.getDateFin().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        resp.setStatut(saved.getStatut());
        resp.setChambreId(ch.getId());
        resp.setNbAdultes(dto.getNbAdultes());
        resp.setNbEnfants(dto.getNbEnfants());
        resp.setNbChambres(dto.getNbChambres());
        resp.setSubtotal(quote.getSubtotal());
        resp.setTaxes(quote.getTaxes());
        resp.setFrais(quote.getFrais());
        resp.setTotal(quote.getTotal());
        resp.setCurrency(quote.getCurrency());
        return resp;
    }

    /* ====================== LISTE PAR HÔTEL ====================== */

    public Page<Reservation> listerParHotel(Long hotelId, Statut statut, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (statut != null) {
            return reservationRepository.findEnAttenteByHotelAndStatut(hotelId, statut, pageable);
        }
        return reservationRepository.findByHotelId(hotelId, pageable);
    }

    public Page<Reservation> listerEnAttenteParHotel(Long hotelId, int page, int size) {
        return listerParHotel(hotelId, Statut.EN_ATTENTE, page, size);
    }

    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation non trouvée"));
    }
    @Transactional
    public void checkout(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Réservation non trouvée"));

        if (r.getChambre() != null) {
            Chambre ch = r.getChambre();
            ch.setEtatChambre(EtatChambre.DIRTY);
            ch.setLastCleanedAt(null);
        }

        r.setDateTime(new Date());

        reservationRepository.save(r);


    }

}
