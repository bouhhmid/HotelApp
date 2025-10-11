package tn.esprit.gestionhotilere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestionhotilere.entity.ChambreOption;

import java.util.List;

public interface ChambreOptionRepository extends JpaRepository<ChambreOption, Long> {
    List<ChambreOption>findByChambreIdAndActifTrue(Long chambreId);

}
