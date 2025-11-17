package tn.esprit.gestionhotilere.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.gestionhotilere.entity.Reservation;
import tn.esprit.gestionhotilere.entity.Statut;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /* =========================
       Chevauchements (disponibilité)
       ========================= */

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.chambre.id = :chambreId
          AND r.statut IN (tn.esprit.gestionhotilere.entity.Statut.EN_ATTENTE,
                           tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE)
          AND r.dateDebut < :dateFin
          AND r.dateFin   > :dateDebut
    """)
    long countOverlapsChambre(@Param("chambreId") Long chambreId,
                              @Param("dateDebut") Date dateDebut,
                              @Param("dateFin") Date dateFin);

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.serviceHotel.id = :serviceId
          AND r.statut IN (tn.esprit.gestionhotilere.entity.Statut.EN_ATTENTE,
                           tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE)
          AND r.dateDebut < :dateFin
          AND r.dateFin   > :dateDebut
    """)
    long countOverlapsService(@Param("serviceId") Long serviceId,
                              @Param("dateDebut") Date dateDebut,
                              @Param("dateFin") Date dateFin);

    /* =========================
       Accès simple client
       ========================= */

    Optional<Reservation> findByClientIdAndChambreIdAndDateDebut(Long clientId, Long chambreId, Date dateDebut);

    Optional<Reservation> findByClientIdAndServiceHotelIdAndDateDebut(Long clientId, Long serviceHotelId, Date dateDebut);

    Page<Reservation> findByClientId(Long clientId, Pageable pageable);

    Page<Reservation> findByClientIdAndStatut(Long clientId, Statut statut, Pageable pageable);

    Page<Reservation> findByStatut(Statut statut, Pageable pageable);

    /* =========================
       Périodes par chambre
       ========================= */

    @Query("""
      SELECT r FROM Reservation r
      WHERE r.chambre.id = :chambreId
        AND r.dateFin   >= :start
        AND r.dateDebut <= :end
    """)
    List<Reservation> findReservationsInRange(@Param("chambreId") Long chambreId,
                                              @Param("start") Date start,
                                              @Param("end") Date end);

    @Query("""
      SELECT r.dateDebut, r.dateFin
      FROM Reservation r
      WHERE r.chambre.id = :chambreId
        AND r.statut IN (tn.esprit.gestionhotilere.entity.Statut.EN_ATTENTE,
                         tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE)
    """)
    List<Object[]> findReservationsPeriods(@Param("chambreId") Long chambreId);

    /* =========================
       KPIs côté hôtel (chambres)
       ========================= */

    @Query("""
      SELECT COUNT(r)
        FROM Reservation r
       WHERE r.chambre.hotel.id = :hotelId
         AND :today BETWEEN r.dateDebut AND r.dateFin
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
    """)
    long countChambresOccupeesJour(@Param("hotelId") Long hotelId, @Param("today") Date today);

    @Query("""
      SELECT COUNT(r)
        FROM Reservation r
       WHERE r.chambre.hotel.id = :hotelId
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.EN_ATTENTE
    """)
    long countReservationsEnAttente(@Param("hotelId") Long hotelId);

    @Query("""
      SELECT COUNT(r)
        FROM Reservation r
       WHERE r.chambre.hotel.id = :hotelId
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
         AND FUNCTION('date', r.dateDebut) = FUNCTION('date', :today)
    """)
    long countReservationsJour(@Param("hotelId") Long hotelId, @Param("today") Date today);

    /* =========================
       KPIs globaux hôtel (chambre OU service)
       =========================
       ⚠ LEFT JOIN pour ne pas perdre les lignes quand un des liens est nul.
    */

    @Query("""
      SELECT FUNCTION('date', r.dateReservation) as d, COUNT(r)
        FROM Reservation r
        LEFT JOIN r.chambre ch
        LEFT JOIN ch.hotel hc
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hs
       WHERE (hc.id = :hotelId OR hs.id = :hotelId)
         AND r.dateReservation >= :from
       GROUP BY FUNCTION('date', r.dateReservation)
       ORDER BY FUNCTION('date', r.dateReservation)
    """)
    List<Object[]> countReservationsLastDays(@Param("hotelId") Long hotelId, @Param("from") Date from);

    @Query("""
      SELECT r
        FROM Reservation r
       WHERE r.chambre.hotel.id = :hotelId
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
         AND r.dateDebut < :end AND r.dateFin > :start
    """)
    List<Reservation> findConfirmedOverlapping(@Param("hotelId") Long hotelId,
                                               @Param("start") Date start,
                                               @Param("end") Date end);

    @Query("""
      SELECT COUNT(r)
        FROM Reservation r
        LEFT JOIN r.chambre ch
        LEFT JOIN ch.hotel hc
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hs
       WHERE (hc.id = :hotelId OR hs.id = :hotelId)
         AND r.dateReservation BETWEEN :start AND :end
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.ANNULEE
    """)
    long countCancelledInRange(@Param("hotelId") Long hotelId,
                               @Param("start") Date start,
                               @Param("end") Date end);

    @Query("""
      SELECT COUNT(r)
        FROM Reservation r
        LEFT JOIN r.chambre ch
        LEFT JOIN ch.hotel hc
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hs
       WHERE (hc.id = :hotelId OR hs.id = :hotelId)
         AND r.dateReservation BETWEEN :start AND :end
    """)
    long countAllInRange(@Param("hotelId") Long hotelId,
                         @Param("start") Date start,
                         @Param("end") Date end);

    @Query("""
      SELECT FUNCTION('date', r.dateDebut) as d, COUNT(r)
        FROM Reservation r
       WHERE r.chambre.hotel.id = :hotelId
         AND r.dateDebut >= :from
         AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
       GROUP BY FUNCTION('date', r.dateDebut)
       ORDER BY FUNCTION('date', r.dateDebut)
    """)
    List<Object[]> countCheckinsLastDays(@Param("hotelId") Long hotelId, @Param("from") Date from);

    /* =========================
       Listes/pagination
       ========================= */

    Page<Reservation> findByChambre_Hotel_IdAndStatut(Long hotelId, Statut statut, Pageable pageable);

    @Query("""
      SELECT r
        FROM Reservation r
        LEFT JOIN r.chambre c
        LEFT JOIN r.serviceHotel s
        LEFT JOIN c.hotel h
        LEFT JOIN s.hotel hs
       WHERE r.statut = :statut
         AND (h.admnistrateur.id = :adminId OR hs.admnistrateur.id = :adminId)
    """)
    Page<Reservation> findByHotelAdminIdAndStatut(@Param("adminId") Long adminId,
                                                  @Param("statut") Statut statut,
                                                  Pageable pageable);

    @Query("""
      SELECT r
        FROM Reservation r
        LEFT JOIN r.chambre ch
        LEFT JOIN ch.hotel hc
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hs
       WHERE (hc.id = :hotelId OR hs.id = :hotelId)
    """)
    Page<Reservation> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

    @Query("""
      SELECT r
        FROM Reservation r
        LEFT JOIN r.chambre c
        LEFT JOIN c.hotel hc
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hs
       WHERE r.statut = :statut
         AND (hc.id = :hotelId OR hs.id = :hotelId)
    """)
    Page<Reservation> findEnAttenteByHotelAndStatut(@Param("hotelId") Long hotelId,
                                                    @Param("statut") Statut statut,
                                                    Pageable pageable);

    /* =========================
       Guards d’accès (ownership)
       =========================
       ⚠ LEFT JOIN + tests de nullité pour couvrir chambre OU service.
    */

    @Query("""
      SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservation r
        LEFT JOIN r.chambre ch
        LEFT JOIN ch.hotel hch
        LEFT JOIN r.serviceHotel sh
        LEFT JOIN sh.hotel hsh
       WHERE r.id = :resId
         AND (
              (ch IS NOT NULL AND hch.admnistrateur.id = :adminId)
           OR (sh IS NOT NULL AND hsh.admnistrateur.id = :adminId)
         )
    """)
    boolean adminOwnsReservation(@Param("resId") Long resId, @Param("adminId") Long adminId);

    @Query("""
      SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END
        FROM Reservation r
       WHERE r.id = :resId AND r.client.id = :clientId
    """)
    boolean clientOwnsReservation(@Param("resId") Long resId, @Param("clientId") Long clientId);

    @Query("""
  SELECT COUNT(r)
    FROM Reservation r
   WHERE r.chambre.hotel.id = :hotelId
     AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
     AND FUNCTION('date', r.dateFin) = FUNCTION('date', :today)
""")
    long countCheckoutsJour(@Param("hotelId") Long hotelId, @Param("today") Date today);

    /* =========================
   Housekeeping (jour J)
   ========================= */

    /** Départs aujourd'hui (chambre) : dateFin = today, statut CONFIRMEE */
    @Query("""
  SELECT r
    FROM Reservation r
   WHERE r.chambre.hotel.id = :hotelId
     AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
     AND FUNCTION('date', r.dateFin) = FUNCTION('date', :today)
""")
    List<Reservation> findDeparturesOnDate(@Param("hotelId") Long hotelId,
                                           @Param("today") Date today);

    /** Arrivées aujourd'hui (chambre) : dateDebut = today, statut EN_ATTENTE ou CONFIRMEE */
    @Query("""
  SELECT r
    FROM Reservation r
   WHERE r.chambre.hotel.id = :hotelId
     AND r.statut IN (
       tn.esprit.gestionhotilere.entity.Statut.EN_ATTENTE,
       tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
     )
     AND FUNCTION('date', r.dateDebut) = FUNCTION('date', :today)
""")
    List<Reservation> findArrivalsOnDate(@Param("hotelId") Long hotelId,
                                         @Param("today") Date today);

    /** Stayovers aujourd'hui : today ∈ [dateDebut, dateFin) et statut CONFIRMEE */
    @Query("""
  SELECT r
    FROM Reservation r
   WHERE r.chambre.hotel.id = :hotelId
     AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
     AND FUNCTION('date', :today) >= FUNCTION('date', r.dateDebut)
     AND FUNCTION('date', :today) <  FUNCTION('date', r.dateFin)
""")
    List<Reservation> findStayoversOnDate(@Param("hotelId") Long hotelId,
                                          @Param("today") Date today);

}
