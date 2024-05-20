package com.DA.DA.controller;


import com.DA.DA.dto.infoDTOConjonture;
import com.DA.DA.entite.NoteConjonctureIARDTemporaire;
import com.DA.DA.entite.Note_ConjonctureIARD;
import com.DA.DA.entite.*;
import com.DA.DA.repository.MessagerieRepository;
import com.DA.DA.repository.Note_ConjonctureIARDRepository;
import com.DA.DA.repository.Note_ConjonctureIARD_temporaireRepository;
import com.DA.DA.repository.StatutRepository;
import com.DA.DA.security.JwtService;

import com.DA.DA.security.JwtServiceMembre;
import com.DA.DA.service.MessageService;
import com.DA.DA.service.Note_ConjonctureIARDService;
import com.DA.DA.service.Note_ConjonctureIARD_temporaireService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/IARD")
public class Note_ConjonctureIARDController {
    private JwtServiceMembre jwtServiceMembre;
    private  Note_ConjonctureIARDService noteConjonctureIARDService;
    private Note_ConjonctureIARD_temporaireRepository note_conjonctureIARD_temporaireRepository;
    private Note_ConjonctureIARD_temporaireService noteConjonctureIARDTemporaireService;
    private MessagerieRepository messagerieRepository;


    //API pour creer une note de conjoncture par un menbre d'une compagnie assurance IARD dans la table temporaire

    @PostMapping("/creer")
    public ResponseEntity<String> createOrUpdateNoteConjonctureIARDTemporaire(@RequestBody NoteConjonctureIARDTemporaire note) {
        try {
            // Appel de la méthode de service sans avoir besoin de récupérer le token JWT ici
            noteConjonctureIARDTemporaireService.createOrUpdateNoteConjonctureIARDTemporaire(note);
            return ResponseEntity.ok("Note de conjoncture créée avec succès.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Une erreur est survenue lors de la création ou de la mise à jour de la note de conjoncture.");
        }
    }




    //API pour creer une note de conjoncture IARD finale
    //la note est crée lorsqu'un menbre de la DA valide une note de conjoncture
    @PostMapping("/final/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<?> creerFinal(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        Long newStatutId = Long.parseLong(requestBody.get("newStatutId").toString());
        String contenuMessage = requestBody.containsKey("contenuMessage") ? requestBody.get("contenuMessage").toString() : null;

        return noteConjonctureIARDTemporaireService.creerFinal(dateSoumission, nom, emailMembre,newStatutId, contenuMessage, request);
    }
    @GetMapping("/ConjonctureIARDrelle")

    public ResponseEntity<List<infoDTOConjonture>> getAllConjoncturesIARD() {
        List<infoDTOConjonture> dtoList = noteConjonctureIARDService.getAllConjonctureIARDDTOs();
        if (dtoList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/ConjonctureIARDtemporaire")
    public ResponseEntity<List<infoDTOConjonture>> getAllConjoncturesIARDtemporaire() {
        List<infoDTOConjonture> dtoList = noteConjonctureIARDTemporaireService.getAllConjonctureIARDDTOs();
        if (dtoList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dtoList);
    }

    //pour l'admin dans la vraie base de donnée

    @GetMapping("/rechercheParDateSoumission/{date}")
    public ResponseEntity<?> rechercheParDateSoumissionCompagnie(
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureIARD> notes =noteConjonctureIARDService.trouverParDateSoumission(dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>("Aucune note de conjoncture trouvée pour la date fournie.", HttpStatus.NOT_FOUND);
        }
        List<infoDTOConjonture> dtoList = notes.stream()
                .map(note -> new infoDTOConjonture(

                        note.getDateSoumission(),
                        note.getAnnee(),
                        note.getTrimestre(),
                        note.getNombre_de_contrats_assurance(),
                        note.getPrime_emise_nette_annulations(),
                        note.getMaladie_prime_emise(),
                        note.getAccidents_corporels_prime_emise(),
                        note.getRc_automobile_prime_emise(),
                        note.getAutres_risques_automobiles_prime_emise(),
                        note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                        note.getCredit_prime_emise(),
                        note.getCaution_prime_emise(),
                        note.getAssurance_importation_des_marchandises_prime_emise(),
                        note.getMicroassurance_prime_emise(),
                        note.getAssurances_indicielles_prime_emise(),
                        note.getAutres_risques_dommages_prime_emise(),
                        note.getAcceptations_dommages(),
                        note.getPrime_cedee_en_reassurance(),
                        note.getSinistre_a_payees(),
                        note.getStock_de_sinistres_bons_a_payer(),
                        note.getMaladie_stock(),
                        note.getAccidents_corporels_stock(),
                        note.getRc_automobile_stock(),
                        note.getAutres_risques_automobiles_stock(),
                        note.getIncendie_et_autres_dommages_aux_biens_stock(),
                        note.getCredit_stock(),
                        note.getCaution_stock(),
                        note.getAssurance_importation_des_marchandises_stock(),
                        note.getMicroassurance_stock(),
                        note.getAssurances_indicielles_stock(),
                        note.getAutres_risques_dommages_stock(),
                        note.getDont_sisnistre_payee(),
                        note.getRecours_encaisser(),
                        note.getRecours_effectivement_encaisser(),
                        note.getProduction_des_courtiers(),
                        note.getCommissions_servies_aux_courtiers(),
                        note.getProduction_autres_intermediaires(),
                        note.getCommissions_servies_aux_autres_intermediaires(),
                        note.getCreances_sur_etat(),
                        note.getPrime_impayee(),
                        note.getEmprunts_public_echus(),
                        note.getFrais_du_personnel(),
                        note.getNombre_hommes(),
                        note.getNombre_femmes(),
                        note.getEffectif_du_personnel(),
                        note.getAutres_charges(),
                        note.getMembreAssurance().getSociete().getNom(),
                        note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                        note.getStatut().getEtat(),
                        note.getMembreAssurance().getNom(),
                        note.getMembreAssurance().getPrenoms(),
                        note.getMembreAssurance().getGenre(),
                        note.getMembreAssurance().getPoste(),
                        note.getMembreAssurance().getEmail()


                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/recherchePaSociete/{nom}")
    public ResponseEntity<?> rechercheParSocietes(
            @PathVariable("nom") String nom) {
        List<Note_ConjonctureIARD> notes =noteConjonctureIARDService.trouverParNomsociete(nom);
        if (notes.isEmpty()) {
            return new ResponseEntity<>("Aucune note de conjoncture trouvée pour la date fournie.", HttpStatus.NOT_FOUND);
        }
        List<infoDTOConjonture> dtoList = notes.stream()
                .map(note -> new infoDTOConjonture(

                        note.getDateSoumission(),
                        note.getAnnee(),
                        note.getTrimestre(),
                        note.getNombre_de_contrats_assurance(),
                        note.getPrime_emise_nette_annulations(),
                        note.getMaladie_prime_emise(),
                        note.getAccidents_corporels_prime_emise(),
                        note.getRc_automobile_prime_emise(),
                        note.getAutres_risques_automobiles_prime_emise(),
                        note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                        note.getCredit_prime_emise(),
                        note.getCaution_prime_emise(),
                        note.getAssurance_importation_des_marchandises_prime_emise(),
                        note.getMicroassurance_prime_emise(),
                        note.getAssurances_indicielles_prime_emise(),
                        note.getAutres_risques_dommages_prime_emise(),
                        note.getAcceptations_dommages(),
                        note.getPrime_cedee_en_reassurance(),
                        note.getSinistre_a_payees(),
                        note.getStock_de_sinistres_bons_a_payer(),
                        note.getMaladie_stock(),
                        note.getAccidents_corporels_stock(),
                        note.getRc_automobile_stock(),
                        note.getAutres_risques_automobiles_stock(),
                        note.getIncendie_et_autres_dommages_aux_biens_stock(),
                        note.getCredit_stock(),
                        note.getCaution_stock(),
                        note.getAssurance_importation_des_marchandises_stock(),
                        note.getMicroassurance_stock(),
                        note.getAssurances_indicielles_stock(),
                        note.getAutres_risques_dommages_stock(),
                        note.getDont_sisnistre_payee(),
                        note.getRecours_encaisser(),
                        note.getRecours_effectivement_encaisser(),
                        note.getProduction_des_courtiers(),
                        note.getCommissions_servies_aux_courtiers(),
                        note.getProduction_autres_intermediaires(),
                        note.getCommissions_servies_aux_autres_intermediaires(),
                        note.getCreances_sur_etat(),
                        note.getPrime_impayee(),
                        note.getEmprunts_public_echus(),
                        note.getFrais_du_personnel(),
                        note.getNombre_hommes(),
                        note.getNombre_femmes(),
                        note.getEffectif_du_personnel(),
                        note.getAutres_charges(),
                        note.getMembreAssurance().getSociete().getNom(),
                        note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                        note.getStatut().getEtat(),
                        note.getMembreAssurance().getNom(),
                        note.getMembreAssurance().getPrenoms(),
                        note.getMembreAssurance().getGenre(),
                        note.getMembreAssurance().getPoste(),
                        note.getMembreAssurance().getEmail()

                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
    //pour l'employee dans la base de donnée temporaire

    @GetMapping("/rechercheParDateSoumissionEmployee/{date}")
    public ResponseEntity<?> rechercheParDateSoumissionSocieteEmploye(
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureIARDTemporaire> notes =noteConjonctureIARDTemporaireService.trouverParDateSoumission(dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>("Aucune note de conjoncture trouvée pour la date fournie.", HttpStatus.NOT_FOUND);
        }
        List<infoDTOConjonture> dtoList = notes.stream()
                .map(note -> new infoDTOConjonture(

                        note.getDateSoumission(),
                        note.getAnnee(),
                        note.getTrimestre(),
                        note.getNombre_de_contrats_assurance(),
                        note.getPrime_emise_nette_annulations(),
                        note.getMaladie_prime_emise(),
                        note.getAccidents_corporels_prime_emise(),
                        note.getRc_automobile_prime_emise(),
                        note.getAutres_risques_automobiles_prime_emise(),
                        note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                        note.getCredit_prime_emise(),
                        note.getCaution_prime_emise(),
                        note.getAssurance_importation_des_marchandises_prime_emise(),
                        note.getMicroassurance_prime_emise(),
                        note.getAssurances_indicielles_prime_emise(),
                        note.getAutres_risques_dommages_prime_emise(),
                        note.getAcceptations_dommages(),
                        note.getPrime_cedee_en_reassurance(),
                        note.getSinistre_a_payees(),
                        note.getStock_de_sinistres_bons_a_payer(),
                        note.getMaladie_stock(),
                        note.getAccidents_corporels_stock(),
                        note.getRc_automobile_stock(),
                        note.getAutres_risques_automobiles_stock(),
                        note.getIncendie_et_autres_dommages_aux_biens_stock(),
                        note.getCredit_stock(),
                        note.getCaution_stock(),
                        note.getAssurance_importation_des_marchandises_stock(),
                        note.getMicroassurance_stock(),
                        note.getAssurances_indicielles_stock(),
                        note.getAutres_risques_dommages_stock(),
                        note.getDont_sisnistre_payee(),
                        note.getRecours_encaisser(),
                        note.getRecours_effectivement_encaisser(),
                        note.getProduction_des_courtiers(),
                        note.getCommissions_servies_aux_courtiers(),
                        note.getProduction_autres_intermediaires(),
                        note.getCommissions_servies_aux_autres_intermediaires(),
                        note.getCreances_sur_etat(),
                        note.getPrime_impayee(),
                        note.getEmprunts_public_echus(),
                        note.getFrais_du_personnel(),
                        note.getNombre_hommes(),
                        note.getNombre_femmes(),
                        note.getEffectif_du_personnel(),
                        note.getAutres_charges(),
                        note.getMembreAssurance().getSociete().getNom(),
                        note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                        note.getStatut().getEtat(),
                        note.getMembreAssurance().getNom(),
                        note.getMembreAssurance().getPrenoms(),
                        note.getMembreAssurance().getGenre(),
                        note.getMembreAssurance().getPoste(),
                        note.getMembreAssurance().getEmail()

                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/rechercheParSocieteEMPLOYE/{nom}")
    public ResponseEntity<?> rechercheParSociete(
            @PathVariable("nom") String nom) {
        List<NoteConjonctureIARDTemporaire> notes =noteConjonctureIARDTemporaireService.trouverParNomsociet(nom);
        if (notes.isEmpty()) {
            return new ResponseEntity<>("Aucune note de conjoncture trouvée pour le nom fourni.", HttpStatus.NOT_FOUND);
        }
        List<infoDTOConjonture> dtoList = notes.stream()
                .map(note -> new infoDTOConjonture(
                        note.getDateSoumission(),
                        note.getAnnee(),
                        note.getTrimestre(),
                        note.getNombre_de_contrats_assurance(),
                        note.getPrime_emise_nette_annulations(),
                        note.getMaladie_prime_emise(),
                        note.getAccidents_corporels_prime_emise(),
                        note.getRc_automobile_prime_emise(),
                        note.getAutres_risques_automobiles_prime_emise(),
                        note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                        note.getCredit_prime_emise(),
                        note.getCaution_prime_emise(),
                        note.getAssurance_importation_des_marchandises_prime_emise(),
                        note.getMicroassurance_prime_emise(),
                        note.getAssurances_indicielles_prime_emise(),
                        note.getAutres_risques_dommages_prime_emise(),
                        note.getAcceptations_dommages(),
                        note.getPrime_cedee_en_reassurance(),
                        note.getSinistre_a_payees(),
                        note.getStock_de_sinistres_bons_a_payer(),
                        note.getMaladie_stock(),
                        note.getAccidents_corporels_stock(),
                        note.getRc_automobile_stock(),
                        note.getAutres_risques_automobiles_stock(),
                        note.getIncendie_et_autres_dommages_aux_biens_stock(),
                        note.getCredit_stock(),
                        note.getCaution_stock(),
                        note.getAssurance_importation_des_marchandises_stock(),
                        note.getMicroassurance_stock(),
                        note.getAssurances_indicielles_stock(),
                        note.getAutres_risques_dommages_stock(),
                        note.getDont_sisnistre_payee(),
                        note.getRecours_encaisser(),
                        note.getRecours_effectivement_encaisser(),
                        note.getProduction_des_courtiers(),
                        note.getCommissions_servies_aux_courtiers(),
                        note.getProduction_autres_intermediaires(),
                        note.getCommissions_servies_aux_autres_intermediaires(),
                        note.getCreances_sur_etat(),
                        note.getPrime_impayee(),
                        note.getEmprunts_public_echus(),
                        note.getFrais_du_personnel(),
                        note.getNombre_hommes(),
                        note.getNombre_femmes(),
                        note.getEffectif_du_personnel(),
                        note.getAutres_charges(),
                        note.getMembreAssurance().getSociete().getNom(),
                        note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                        note.getStatut().getEtat(),
                        note.getMembreAssurance().getNom(),
                        note.getMembreAssurance().getPrenoms(),
                        note.getMembreAssurance().getGenre(),
                        note.getMembreAssurance().getPoste(),
                        note.getMembreAssurance().getEmail()


                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
    @GetMapping("/rechercheNoteParNomSociete")
    public ResponseEntity<List<infoDTOConjonture>> trouverParNomCompagnie(HttpServletRequest request) {
        // Extraire le nom de la compagnie du token
        String token = request.getHeader("Authorization").substring(7); // Supprimer "Bearer "
        String nomCompagnie = jwtServiceMembre.lireNomCompagnieDuToken(token);

        // Vérifier si le nom de la compagnie est récupéré avec succès
        if (nomCompagnie == null || nomCompagnie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Ou une autre réponse appropriée
        }

        List<NoteConjonctureIARDTemporaire> notes = noteConjonctureIARDTemporaireService.trouverParNomCompagnie(nomCompagnie);

        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<infoDTOConjonture> dtoList = notes.stream()
                    .map(note -> new infoDTOConjonture(
                            note.getDateSoumission(),
                            note.getAnnee(),
                            note.getTrimestre(),
                            note.getNombre_de_contrats_assurance(),
                            note.getPrime_emise_nette_annulations(),
                            note.getMaladie_prime_emise(),
                            note.getAccidents_corporels_prime_emise(),
                            note.getRc_automobile_prime_emise(),
                            note.getAutres_risques_automobiles_prime_emise(),
                            note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                            note.getCredit_prime_emise(),
                            note.getCaution_prime_emise(),
                            note.getAssurance_importation_des_marchandises_prime_emise(),
                            note.getMicroassurance_prime_emise(),
                            note.getAssurances_indicielles_prime_emise(),
                            note.getAutres_risques_dommages_prime_emise(),
                            note.getAcceptations_dommages(),
                            note.getPrime_cedee_en_reassurance(),
                            note.getSinistre_a_payees(),
                            note.getStock_de_sinistres_bons_a_payer(),
                            note.getMaladie_stock(),
                            note.getAccidents_corporels_stock(),
                            note.getRc_automobile_stock(),
                            note.getAutres_risques_automobiles_stock(),
                            note.getIncendie_et_autres_dommages_aux_biens_stock(),
                            note.getCredit_stock(),
                            note.getCaution_stock(),
                            note.getAssurance_importation_des_marchandises_stock(),
                            note.getMicroassurance_stock(),
                            note.getAssurances_indicielles_stock(),
                            note.getAutres_risques_dommages_stock(),
                            note.getDont_sisnistre_payee(),
                            note.getRecours_encaisser(),
                            note.getRecours_effectivement_encaisser(),
                            note.getProduction_des_courtiers(),
                            note.getCommissions_servies_aux_courtiers(),
                            note.getProduction_autres_intermediaires(),
                            note.getCommissions_servies_aux_autres_intermediaires(),
                            note.getCreances_sur_etat(),
                            note.getPrime_impayee(),
                            note.getEmprunts_public_echus(),
                            note.getFrais_du_personnel(),
                            note.getNombre_hommes(),
                            note.getNombre_femmes(),
                            note.getEffectif_du_personnel(),
                            note.getAutres_charges(),
                            note.getMembreAssurance().getSociete().getNom(),
                            note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                            note.getStatut().getEtat(),
                            note.getMembreAssurance().getNom(),
                            note.getMembreAssurance().getPrenoms(),
                            note.getMembreAssurance().getGenre(),
                            note.getMembreAssurance().getPoste(),
                            note.getMembreAssurance().getEmail()


                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }

    }




}

