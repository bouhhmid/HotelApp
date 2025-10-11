package tn.esprit.gestionhotilere.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tn.esprit.gestionhotilere.dto.ChambreDetailsDTO;
import tn.esprit.gestionhotilere.dto.ChambreOptionDTO;
import tn.esprit.gestionhotilere.dto.ChambrecreateDTO;
import tn.esprit.gestionhotilere.entity.*;
import tn.esprit.gestionhotilere.exception.BusinessException;
import tn.esprit.gestionhotilere.repository.ChambreRepository;
import tn.esprit.gestionhotilere.repository.HotelRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChambreService {

    private final ChambreRepository chambreRepository;
    private final HotelRepository hotelRepository;
    private final UserService userService;

    /** Vérifie que l'utilisateur est administrateur de l'hôtel */
    private void verifierAdminHotel(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel introuvable"));

        User utilisateur = userService.getOrCreateCurrentUser();

        if (utilisateur.getRole() != Role.SUPERADMIN &&
                !hotel.getAdmnistrateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Accès refusé : vous n'êtes pas administrateur de cet hôtel.");
        }
    }

    /** Ajouter une chambre avec ses options */
    @Transactional
    public ChambreDetailsDTO ajouterChambre(Long hotelId, @Valid ChambrecreateDTO dto) {
        // 1) Sécurité: seul l’admin de l’hôtel peut créer
        verifierAdminHotel(hotelId);

        // 2) Hôtel
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Hôtel introuvable"));

        // 3) Règle: au moins une option
        List<ChambreOptionDTO> optionDTOs = (dto.getOptions() == null)
                ? Collections.emptyList()
                : dto.getOptions();

        if (optionDTOs.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Au moins une option de chambre est requise.");
        }

        // 4) Construire la chambre
        Chambre chambre = new Chambre();
        chambre.setNumero(dto.getNumero());
        chambre.setTypeChambre(dto.getTypeChambre());
        chambre.setDispo(true);
        chambre.setDescription(dto.getDescription());
        chambre.setImageUrl(dto.getImageUrl());
        chambre.setCapaciteAdulte(dto.getCapaciteAdulte());
        chambre.setCapaciteEnfant(dto.getCapaciteEnfant());
        chambre.setHotel(hotel);

        // 5) Déterminer le prix vitrine (priorité à la 1ère option ACTIVE)
        ChambreOptionDTO firstActive = optionDTOs.stream()
                .filter(ChambreOptionDTO::isActif)
                .findFirst()
                .orElse(optionDTOs.get(0));

        chambre.setPrixBase(firstActive.getPrixParNuitParPersonne());

        List<ChambreOption> options = optionDTOs.stream()
                .map(optDto -> {
                    ChambreOption opt = new ChambreOption();
                    opt.setChambre(chambre);
                    opt.setTypeChambre(optDto.getTypeChambre());
                    opt.setVue(optDto.getVue());
                    opt.setPrixParNuitParPersonne(optDto.getPrixParNuitParPersonne());
                    opt.setActif(optDto.isActif());
                    return opt;
                })
                .collect(Collectors.toList());

        chambre.setOptions(options);

        // 7) Persister
        Chambre saved = chambreRepository.save(chambre);

        // 8) Retour DTO
        return mapToDetailsDTO(saved);
    }
    /** Modifier une chambre */
    @Transactional
    public ChambreDetailsDTO modifierChambre(Long chambreId, ChambrecreateDTO dto) {
        Chambre existing = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new RuntimeException("Chambre introuvable"));

        verifierAdminHotel(existing.getHotel().getId());

        existing.setNumero(dto.getNumero());
        existing.setTypeChambre(dto.getTypeChambre());
        existing.setDescription(dto.getDescription());
        existing.setImageUrl(dto.getImageUrl());
        existing.setCapaciteAdulte(dto.getCapaciteAdulte());
        existing.setCapaciteEnfant(dto.getCapaciteEnfant());
        existing.setPrixBase(dto.getOptions().get(0).getPrixParNuitParPersonne());

        // Remplacer les options
        existing.getOptions().clear();
        List<ChambreOption> newOptions = dto.getOptions().stream()
                .map(optDto -> {
                    ChambreOption opt = new ChambreOption();
                    opt.setChambre(existing);
                    opt.setTypeChambre(optDto.getTypeChambre());
                    opt.setVue(optDto.getVue());
                    opt.setPrixParNuitParPersonne(optDto.getPrixParNuitParPersonne());
                    opt.setActif(optDto.isActif());
                    return opt;
                })
                .collect(Collectors.toList());
        existing.getOptions().addAll(newOptions);

        Chambre saved = chambreRepository.save(existing);
        return mapToDetailsDTO(saved);
    }

    /** Supprimer une chambre */
    public void supprimerChambre(Long chambreId) {
        Chambre chambre = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new RuntimeException("Chambre introuvable"));
        verifierAdminHotel(chambre.getHotel().getId());
        chambreRepository.delete(chambre);
    }

    /** Récupérer toutes les chambres d'un hôtel */
    public List<ChambreDetailsDTO> getChambresByHotel(Long hotelId) {
        return chambreRepository.findByHotelId(hotelId)
                .stream()
                .map(this::mapToDetailsDTO)
                .collect(Collectors.toList());
    }

    /** Récupérer une chambre par ID */
    public ChambreDetailsDTO getChambreById(Long id) {
        Chambre chambre = chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre introuvable"));
        return mapToDetailsDTO(chambre);
    }

    /** Mapper entité → DTO */
    private ChambreDetailsDTO mapToDetailsDTO(Chambre chambre) {
        return ChambreDetailsDTO.builder()
                .id(chambre.getId())
                .numero(chambre.getNumero())
                .typeChambre(chambre.getTypeChambre())
                .dispo(chambre.isDispo())
                .description(chambre.getDescription())
                .imageUrl(chambre.getImageUrl())
                .capaciteAdulte(chambre.getCapaciteAdulte())
                .capaciteEnfant(chambre.getCapaciteEnfant())
                .options(chambre.getOptions().stream()
                        .map(opt -> ChambreOptionDTO.builder()
                                .id(opt.getId())
                                .typeChambre(opt.getTypeChambre())
                                .vue(opt.getVue())
                                .prixParNuitParPersonne(opt.getPrixParNuitParPersonne())
                                .actif(opt.isActif())
                                .build())
                        .collect(Collectors.toList()))
                .etatChambre(chambre.getEtatChambre())
                .lastClenedAt(chambre.getLastCleanedAt())
                .build();
    }
    public Chambre getChambreEntityById(Long id) {
        return chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre introuvable"));
    }
    public void markDirty(Long id){
        var ch = getChambreEntityById(id);
        ch.setEtatChambre(EtatChambre.DIRTY);
        chambreRepository.save(ch);
    }
    public void markClean(Long id){
        var ch = getChambreEntityById(id);
        ch.setEtatChambre(EtatChambre.CLEAN);
        ch.setLastCleanedAt(LocalDateTime.now());
        chambreRepository.save(ch);
    }
}
