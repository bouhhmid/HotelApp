package tn.esprit.gestionhotilere.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import tn.esprit.gestionhotilere.dto.ReviewDTO;
import tn.esprit.gestionhotilere.dto.ReviewDTOIn;
import tn.esprit.gestionhotilere.dto.ReviewAggregateDTO;
import tn.esprit.gestionhotilere.entity.Review;
import tn.esprit.gestionhotilere.repository.ReviewRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repo;

    /** Liste paginée des avis (DTO de sortie) */
    public Page<ReviewDTO> list(Long hotelId, int page, int size) {
        return repo.findByHotelIdOrderByCreatedAtDesc(hotelId, PageRequest.of(page, size))
                .map(r -> new ReviewDTO(
                        r.getId(),
                        r.getUserName(),
                        r.getScore(),
                        r.getTitle(),
                        r.getComment(),
                        r.getCreatedAt()
                ));
    }

    /** Agrégats (moyenne globale, nombre d’avis, moyennes par critère) */
    public ReviewAggregateDTO aggregates(Long hotelId) {
        Double avg = Optional.ofNullable(repo.avgScore(hotelId)).orElse(0.0);
        Long count = repo.countByHotelId(hotelId);
        Object[] arr = repo.avgCriteria(hotelId); // [proprete, emplacement, confort, qualitePrix, wifi]

        Map<String, Double> crit = Map.of(
                "Propreté",     safe(arr, 0),
                "Emplacement",  safe(arr, 1),
                "Confort",      safe(arr, 2),
                "Qualité/prix", safe(arr, 3),
                "Wifi",         safe(arr, 4)
        );
        return new ReviewAggregateDTO(avg, count, crit);
    }

    private Double safe(Object[] a, int i) {
        return (a == null || i < 0 || i >= a.length || a[i] == null)
                ? 0.0
                : ((Number) a[i]).doubleValue();
    }

    /** Création d’un avis (DTO d’entrée -> entité -> DTO de sortie) */
    public ReviewDTO add(Long hotelId, ReviewDTOIn in, String userName) {
        Review r = new Review();
        r.setHotelId(hotelId);
        r.setUserName(userName);

        // Champs principaux
        r.setScore(nz(in.score()));
        r.setTitle(in.title());
        r.setComment(in.comment());
        r.setCreatedAt(Instant.now());

        // Sous-notes (peuvent être null côté client)
        r.setProprete(nz(in.proprete()));
        r.setEmplacement(nz(in.emplacement()));
        r.setConfort(nz(in.confort()));
        r.setQualitePrix(nz(in.qualitePrix()));
        r.setWifi(nz(in.wifi()));

        repo.save(r);

        return new ReviewDTO(
                r.getId(),
                r.getUserName(),
                r.getScore(),
                r.getTitle(),
                r.getComment(),
                r.getCreatedAt()
        );
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
