package com.DA.DA.controller;


import com.DA.DA.dto.InfoDTOConjonctureVie;
import com.DA.DA.entite.Note_ConjonctureVie_temporaire;
import com.DA.DA.entite.*;
import com.DA.DA.repository.MessagerieRepository;
import com.DA.DA.repository.Note_ConjonctureVIERepository;
import com.DA.DA.repository.Note_ConjonctureVIE_temporaireRepository;
import com.DA.DA.repository.StatutRepository;

import com.DA.DA.security.JwtServiceMembre;
import com.DA.DA.service.MessageService;
import com.DA.DA.service.Note_ConjonctureVIEService;
import com.DA.DA.service.Note_ConjonctureVIE_temporaireService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/VIE")

public class Note_ConjonctureVIEController {


    private Note_ConjonctureVIEService noteConjonctureVIEService;
    private JwtServiceMembre jwtServiceMembre;
    private Note_ConjonctureVIE_temporaireService noteConjonctureVIETemporaireService;

    @PostMapping("/creer")
    public ResponseEntity<String> createOrUpdateNoteConjonctureVIETemporaire(@RequestBody Note_ConjonctureVie_temporaire note) {
        try {
            noteConjonctureVIETemporaireService.createOrUpdateNoteConjonctureVIETemporaire(note);
            return ResponseEntity.ok("Note de conjoncture créée avec succès.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Une erreur est survenue lors de la création ou de la mise à jour de la note de conjoncture.");
        }
    }


    @PostMapping("/final/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<?> creerFinal(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        Long newStatutId = Long.parseLong(requestBody.get("newStatutId").toString());
        String contenuMessage = requestBody.containsKey("contenuMessage") ? requestBody.get("contenuMessage").toString() : null;

        return noteConjonctureVIETemporaireService.creerFinal(dateSoumission, nom, emailMembre, newStatutId, contenuMessage, request);
    }



    // Récupérer toutes les notes de la base de données temporaire ou l'admin ne peut que supprimer
    @GetMapping("/ConjonctureVIEreelle")
    public ResponseEntity<List<InfoDTOConjonctureVie>> getAllConjoncture() {
        List<InfoDTOConjonctureVie> conjonctureVieDTOs = noteConjonctureVIEService.getAllConjonctureVieDTOs();
        if (conjonctureVieDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(conjonctureVieDTOs);
    }

    // Récupérer toutes les notes de la base de données temporaire ou l'admin peut valider ou refuser
    @GetMapping("/ConjonctureVIEtemporaire")

    public ResponseEntity<List<InfoDTOConjonctureVie>> getAllConjonctureVieDTOs()  {
        List<InfoDTOConjonctureVie> conjonctureVieDTOTemporaires = noteConjonctureVIETemporaireService.getAllConjonctureVieDTOs();
        if ( conjonctureVieDTOTemporaires.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok( conjonctureVieDTOTemporaires);
    }

    @GetMapping("/rechercheNoteParNomSociete")
    public ResponseEntity<List<InfoDTOConjonctureVie>> trouverParNomCompagnie(HttpServletRequest request) {
        // Extraire le nom de la compagnie du token
        String token = request.getHeader("Authorization").substring(7); // Supprimer "Bearer "
        String nomCompagnie = jwtServiceMembre.lireNomCompagnieDuToken(token);

        // Vérifier si le nom de la compagnie est récupéré avec succès
        if (nomCompagnie == null || nomCompagnie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Ou une autre réponse appropriée
        }

        List<Note_ConjonctureVie_temporaire> notes = noteConjonctureVIETemporaireService.trouverParNomCompagnie(nomCompagnie);

        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureVie> dtoList = notes.stream().map(note -> new InfoDTOConjonctureVie(
                    note.getDateSoumission(),
                    note.getAnnee(),
                    note.getTrimestre(),
                    note.getNombre_contrat_assurance(),
                    note.getPrime_emise_nette_annulations(),
                    note.getAssurance_individuelle_prime(),
                    note.getContrats_cas_vie_prime_emise(),
                    note.getContrats_cas_deces_prime_emise(),
                    note.getContrats_mixtes_prime_emise(),
                    note.getCapitalisation_et_epargne_prime_emise(),
                    note.getComplementaire_prime_emise(),
                    note.getAssurance_collective_prime(),
                    note.getContrats_cas_vie_collective_prime_emise(),
                    note.getContrats_cas_deces_collective_prime_emise(),
                    note.getContrats_mixtes_collective_prime_emise(),
                    note.getCapitalisation_et_epargne_collective_prime_emise(),
                    note.getComplementaire_collective_prime_emise(),
                    note.getMicroassurance_vie_prime_emise(),
                    note.getAcceptations_vie(),
                    note.getPrimes_cedees_en_reassurance(),
                    note.getProvisions_mathematiques(),
                    note.getStock_de_sinistres_et_capitaux_echus(),
                    note.getPrestations_vie_effectivement_payees(),
                    note.getAssurances_individuelles(),
                    note.getContrats_en_cas_de_vie_individuelles_prestation_vie(),
                    note.getContrats_en_cas_de_deces_individuelles_prestation_vie(),
                    note.getContrats_mixtes_individuelles_prestation_vie(),
                    note.getCapitalisation_et_epargne_individuelles_prestation_vie(),
                    note.getComplementaires_individuelles_prestation_vie(),
                    note.getAssurances_collectives(),
                    note.getContrats_en_cas_de_vie_collectives_prestation_vie(),
                    note.getContrats_en_cas_de_deces_collectives_prestation_vie(),
                    note.getContrats_mixtes_collectives_prestation_vie(),
                    note.getCapitalisation_et_epargne_collectives_prestation_vie(),
                    note.getComplementaires_collectives_prestation_vie(),
                    note.getMicroassurance_vie_prestation_vie(),
                    note.getProduction_des_courtiers(),
                    note.getCommissions_servies_courtiers(),
                    note.getProduction_des_autres_intermediaires(),
                    note.getCommissions_servies_autres_intermediaires(),
                    note.getAutres_charges(),
                    note.getDont_frais_du_personnel(),
                    note.getNombre_hommes(),
                    note.getNombre_femmes(),
                    note.getEffectif_du_personnel(),
                    note.getCreances_sur_etat(),
                    note.getPrime_impayee(),
                    note.getEmprunts_publics_echus(),
                    note.getMembreAssurance().getSociete().getNom(),
                    note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                    note.getStatut().getEtat(),
                    note.getMembreAssurance().getNom(),
                    note.getMembreAssurance().getPrenoms(),
                    note.getMembreAssurance().getGenre(),
                    note.getMembreAssurance().getPoste(),
                    note.getMembreAssurance().getEmail()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }
    }
    @GetMapping("/rechercheParDateSoumission/{date}")
    public ResponseEntity<?> rechercheParDateSoumissionCompagnie(
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureVIE> notes = noteConjonctureVIEService.trouverParDateSoumission(dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureVie> dtoList = notes.stream().map(note -> new InfoDTOConjonctureVie(
                    note.getDateSoumission(),
                    note.getAnnee(),
                    note.getTrimestre(),
                    note.getNombre_contrat_assurance(),
                    note.getPrime_emise_nette_annulations(),
                    note.getAssurance_individuelle_prime(),
                    note.getContrats_cas_vie_prime_emise(),
                    note.getContrats_cas_deces_prime_emise(),
                    note.getContrats_mixtes_prime_emise(),
                    note.getCapitalisation_et_epargne_prime_emise(),
                    note.getComplementaire_prime_emise(),
                    note.getAssurance_collective_prime(),
                    note.getContrats_cas_vie_collective_prime_emise(),
                    note.getContrats_cas_deces_collective_prime_emise(),
                    note.getContrats_mixtes_collective_prime_emise(),
                    note.getCapitalisation_et_epargne_collective_prime_emise(),
                    note.getComplementaire_collective_prime_emise(),
                    note.getMicroassurance_vie_prime_emise(),
                    note.getAcceptations_vie(),
                    note.getPrimes_cedees_en_reassurance(),
                    note.getProvisions_mathematiques(),
                    note.getStock_de_sinistres_et_capitaux_echus(),
                    note.getPrestations_vie_effectivement_payees(),
                    note.getAssurances_individuelles(),
                    note.getContrats_en_cas_de_vie_individuelles_prestation_vie(),
                    note.getContrats_en_cas_de_deces_individuelles_prestation_vie(),
                    note.getContrats_mixtes_individuelles_prestation_vie(),
                    note.getCapitalisation_et_epargne_individuelles_prestation_vie(),
                    note.getComplementaires_individuelles_prestation_vie(),
                    note.getAssurances_collectives(),
                    note.getContrats_en_cas_de_vie_collectives_prestation_vie(),
                    note.getContrats_en_cas_de_deces_collectives_prestation_vie(),
                    note.getContrats_mixtes_collectives_prestation_vie(),
                    note.getCapitalisation_et_epargne_collectives_prestation_vie(),
                    note.getComplementaires_collectives_prestation_vie(),
                    note.getMicroassurance_vie_prestation_vie(),
                    note.getProduction_des_courtiers(),
                    note.getCommissions_servies_courtiers(),
                    note.getProduction_des_autres_intermediaires(),
                    note.getCommissions_servies_autres_intermediaires(),
                    note.getAutres_charges(),
                    note.getDont_frais_du_personnel(),
                    note.getNombre_hommes(),
                    note.getNombre_femmes(),
                    note.getEffectif_du_personnel(),
                    note.getCreances_sur_etat(),
                    note.getPrime_impayee(),
                    note.getEmprunts_publics_echus(),
                    note.getMembreAssurance().getSociete().getNom(),
                    note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                    note.getStatut().getEtat(),
                    note.getMembreAssurance().getNom(),
                    note.getMembreAssurance().getPrenoms(),
                    note.getMembreAssurance().getGenre(),
                    note.getMembreAssurance().getPoste(),
                    note.getMembreAssurance().getEmail()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }}


    @GetMapping("/rechercheParSociete/{nom}")
    public ResponseEntity<?> rechercheParSocietes(
            @PathVariable("nom") String nom) {
        List<Note_ConjonctureVIE> notes =noteConjonctureVIEService.trouverParNomsociete(nom);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureVie> dtoList = notes.stream().map(note -> new InfoDTOConjonctureVie(
                    note.getDateSoumission(),
                    note.getAnnee(),
                    note.getTrimestre(),
                    note.getNombre_contrat_assurance(),
                    note.getPrime_emise_nette_annulations(),
                    note.getAssurance_individuelle_prime(),
                    note.getContrats_cas_vie_prime_emise(),
                    note.getContrats_cas_deces_prime_emise(),
                    note.getContrats_mixtes_prime_emise(),
                    note.getCapitalisation_et_epargne_prime_emise(),
                    note.getComplementaire_prime_emise(),
                    note.getAssurance_collective_prime(),
                    note.getContrats_cas_vie_collective_prime_emise(),
                    note.getContrats_cas_deces_collective_prime_emise(),
                    note.getContrats_mixtes_collective_prime_emise(),
                    note.getCapitalisation_et_epargne_collective_prime_emise(),
                    note.getComplementaire_collective_prime_emise(),
                    note.getMicroassurance_vie_prime_emise(),
                    note.getAcceptations_vie(),
                    note.getPrimes_cedees_en_reassurance(),
                    note.getProvisions_mathematiques(),
                    note.getStock_de_sinistres_et_capitaux_echus(),
                    note.getPrestations_vie_effectivement_payees(),
                    note.getAssurances_individuelles(),
                    note.getContrats_en_cas_de_vie_individuelles_prestation_vie(),
                    note.getContrats_en_cas_de_deces_individuelles_prestation_vie(),
                    note.getContrats_mixtes_individuelles_prestation_vie(),
                    note.getCapitalisation_et_epargne_individuelles_prestation_vie(),
                    note.getComplementaires_individuelles_prestation_vie(),
                    note.getAssurances_collectives(),
                    note.getContrats_en_cas_de_vie_collectives_prestation_vie(),
                    note.getContrats_en_cas_de_deces_collectives_prestation_vie(),
                    note.getContrats_mixtes_collectives_prestation_vie(),
                    note.getCapitalisation_et_epargne_collectives_prestation_vie(),
                    note.getComplementaires_collectives_prestation_vie(),
                    note.getMicroassurance_vie_prestation_vie(),
                    note.getProduction_des_courtiers(),
                    note.getCommissions_servies_courtiers(),
                    note.getProduction_des_autres_intermediaires(),
                    note.getCommissions_servies_autres_intermediaires(),
                    note.getAutres_charges(),
                    note.getDont_frais_du_personnel(),
                    note.getNombre_hommes(),
                    note.getNombre_femmes(),
                    note.getEffectif_du_personnel(),
                    note.getCreances_sur_etat(),
                    note.getPrime_impayee(),
                    note.getEmprunts_publics_echus(),
                    note.getMembreAssurance().getSociete().getNom(),
                    note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                    note.getStatut().getEtat(),
                    note.getMembreAssurance().getNom(),
                    note.getMembreAssurance().getPrenoms(),
                    note.getMembreAssurance().getGenre(),
                    note.getMembreAssurance().getPoste(),
                    note.getMembreAssurance().getEmail()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }}


    @GetMapping("/rechercheParDateSoumissionEmployee/{date}")
    public ResponseEntity<?> rechercheParDateSoumissionSocieteEmploye(
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureVie_temporaire> notes = noteConjonctureVIETemporaireService.trouverParDateSoumission(dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureVie> dtoList = notes.stream().map(note -> new InfoDTOConjonctureVie(
                    note.getDateSoumission(),
                    note.getAnnee(),
                    note.getTrimestre(),
                    note.getNombre_contrat_assurance(),
                    note.getPrime_emise_nette_annulations(),
                    note.getAssurance_individuelle_prime(),
                    note.getContrats_cas_vie_prime_emise(),
                    note.getContrats_cas_deces_prime_emise(),
                    note.getContrats_mixtes_prime_emise(),
                    note.getCapitalisation_et_epargne_prime_emise(),
                    note.getComplementaire_prime_emise(),
                    note.getAssurance_collective_prime(),
                    note.getContrats_cas_vie_collective_prime_emise(),
                    note.getContrats_cas_deces_collective_prime_emise(),
                    note.getContrats_mixtes_collective_prime_emise(),
                    note.getCapitalisation_et_epargne_collective_prime_emise(),
                    note.getComplementaire_collective_prime_emise(),
                    note.getMicroassurance_vie_prime_emise(),
                    note.getAcceptations_vie(),
                    note.getPrimes_cedees_en_reassurance(),
                    note.getProvisions_mathematiques(),
                    note.getStock_de_sinistres_et_capitaux_echus(),
                    note.getPrestations_vie_effectivement_payees(),
                    note.getAssurances_individuelles(),
                    note.getContrats_en_cas_de_vie_individuelles_prestation_vie(),
                    note.getContrats_en_cas_de_deces_individuelles_prestation_vie(),
                    note.getContrats_mixtes_individuelles_prestation_vie(),
                    note.getCapitalisation_et_epargne_individuelles_prestation_vie(),
                    note.getComplementaires_individuelles_prestation_vie(),
                    note.getAssurances_collectives(),
                    note.getContrats_en_cas_de_vie_collectives_prestation_vie(),
                    note.getContrats_en_cas_de_deces_collectives_prestation_vie(),
                    note.getContrats_mixtes_collectives_prestation_vie(),
                    note.getCapitalisation_et_epargne_collectives_prestation_vie(),
                    note.getComplementaires_collectives_prestation_vie(),
                    note.getMicroassurance_vie_prestation_vie(),
                    note.getProduction_des_courtiers(),
                    note.getCommissions_servies_courtiers(),
                    note.getProduction_des_autres_intermediaires(),
                    note.getCommissions_servies_autres_intermediaires(),
                    note.getAutres_charges(),
                    note.getDont_frais_du_personnel(),
                    note.getNombre_hommes(),
                    note.getNombre_femmes(),
                    note.getEffectif_du_personnel(),
                    note.getCreances_sur_etat(),
                    note.getPrime_impayee(),
                    note.getEmprunts_publics_echus(),
                    note.getMembreAssurance().getSociete().getNom(),
                    note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                    note.getStatut().getEtat(),
                    note.getMembreAssurance().getNom(),
                    note.getMembreAssurance().getPrenoms(),
                    note.getMembreAssurance().getGenre(),
                    note.getMembreAssurance().getPoste(),
                    note.getMembreAssurance().getEmail()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }}

    @GetMapping("/rechercheParSocieteEMPLOYE/{nom}")
    public ResponseEntity<?> rechercheParSociete(
            @PathVariable("nom") String nom) {
        List<Note_ConjonctureVie_temporaire> notes =noteConjonctureVIETemporaireService.trouverParNomsociet(nom);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureVie> dtoList = notes.stream().map(note -> new InfoDTOConjonctureVie(
                    note.getDateSoumission(),
                    note.getAnnee(),
                    note.getTrimestre(),
                    note.getNombre_contrat_assurance(),
                    note.getPrime_emise_nette_annulations(),
                    note.getAssurance_individuelle_prime(),
                    note.getContrats_cas_vie_prime_emise(),
                    note.getContrats_cas_deces_prime_emise(),
                    note.getContrats_mixtes_prime_emise(),
                    note.getCapitalisation_et_epargne_prime_emise(),
                    note.getComplementaire_prime_emise(),
                    note.getAssurance_collective_prime(),
                    note.getContrats_cas_vie_collective_prime_emise(),
                    note.getContrats_cas_deces_collective_prime_emise(),
                    note.getContrats_mixtes_collective_prime_emise(),
                    note.getCapitalisation_et_epargne_collective_prime_emise(),
                    note.getComplementaire_collective_prime_emise(),
                    note.getMicroassurance_vie_prime_emise(),
                    note.getAcceptations_vie(),
                    note.getPrimes_cedees_en_reassurance(),
                    note.getProvisions_mathematiques(),
                    note.getStock_de_sinistres_et_capitaux_echus(),
                    note.getPrestations_vie_effectivement_payees(),
                    note.getAssurances_individuelles(),
                    note.getContrats_en_cas_de_vie_individuelles_prestation_vie(),
                    note.getContrats_en_cas_de_deces_individuelles_prestation_vie(),
                    note.getContrats_mixtes_individuelles_prestation_vie(),
                    note.getCapitalisation_et_epargne_individuelles_prestation_vie(),
                    note.getComplementaires_individuelles_prestation_vie(),
                    note.getAssurances_collectives(),
                    note.getContrats_en_cas_de_vie_collectives_prestation_vie(),
                    note.getContrats_en_cas_de_deces_collectives_prestation_vie(),
                    note.getContrats_mixtes_collectives_prestation_vie(),
                    note.getCapitalisation_et_epargne_collectives_prestation_vie(),
                    note.getComplementaires_collectives_prestation_vie(),
                    note.getMicroassurance_vie_prestation_vie(),
                    note.getProduction_des_courtiers(),
                    note.getCommissions_servies_courtiers(),
                    note.getProduction_des_autres_intermediaires(),
                    note.getCommissions_servies_autres_intermediaires(),
                    note.getAutres_charges(),
                    note.getDont_frais_du_personnel(),
                    note.getNombre_hommes(),
                    note.getNombre_femmes(),
                    note.getEffectif_du_personnel(),
                    note.getCreances_sur_etat(),
                    note.getPrime_impayee(),
                    note.getEmprunts_publics_echus(),
                    note.getMembreAssurance().getSociete().getNom(),
                    note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                    note.getStatut().getEtat(),
                    note.getMembreAssurance().getNom(),
                    note.getMembreAssurance().getPrenoms(),
                    note.getMembreAssurance().getGenre(),
                    note.getMembreAssurance().getPoste(),
                    note.getMembreAssurance().getEmail()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }}





}


