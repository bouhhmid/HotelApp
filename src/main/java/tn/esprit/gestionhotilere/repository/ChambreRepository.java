package tn.esprit.gestionhotilere.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.gestionhotilere.entity.Chambre;
import tn.esprit.gestionhotilere.entity.Hotel;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;
import tn.esprit.gestionhotilere.entity.TypeChambre;
public interface ChambreRepository extends JpaRepository<Chambre, Long> {

    List<Chambre> findByHotel(Hotel hotel);
    boolean existsByIdAndHotelId(Long chambreId, Long hotelId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chambre c WHERE c.id = :id")
    Chambre lockAndGet(@Param("id") Long id);

    List<Chambre> findByHotelId(Long hotelId);

    // 🔹 AJOUTER :
    long countByHotelId(Long hotelId);

    @Query("""
      SELECT c.typeChambre, COUNT(c)
        FROM Chambre c
       WHERE c.hotel.id = :hotelId
       GROUP BY c.typeChambre
    """)
    List<Object[]> countByType(@Param("hotelId") Long hotelId);
    @Query("""
  SELECT COUNT(DISTINCT r.chambre.id)
    FROM Reservation r
   WHERE r.chambre.hotel.id = :hotelId
     AND :today BETWEEN r.dateDebut AND r.dateFin
     AND r.statut = tn.esprit.gestionhotilere.entity.Statut.CONFIRMEE
""")
    long countChambresOccupeesJour(@Param("hotelId") Long hotelId, @Param("today") Date today);
}