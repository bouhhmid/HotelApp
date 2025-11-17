package tn.esprit.gestionhotilere.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.gestionhotilere.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByHotelIdOrderByCreatedAtDesc(Long hotelId, Pageable pageable);

    @Query("""
     select avg(r.score) from Review r where r.hotelId = :hotelId
  """)
    Double avgScore(Long hotelId);

    @Query("""
     select count(r) from Review r where r.hotelId = :hotelId
  """)
    Long countByHotelId(Long hotelId);

    @Query("""
     select avg(r.proprete), avg(r.emplacement), avg(r.confort), avg(r.qualitePrix), avg(r.wifi)
     from Review r where r.hotelId = :hotelId
  """)
    Object[] avgCriteria(Long hotelId);
}

