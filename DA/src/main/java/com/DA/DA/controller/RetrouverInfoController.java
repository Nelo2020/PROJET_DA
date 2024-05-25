package com.DA.DA.controller;


import com.DA.DA.dto.*;
import com.DA.DA.entite.*;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/RetrouverInfo")
public class RetrouverInfoController {
    private final Note_ConjonctureVIE_temporaireService note_ConjonctureVIE_temporaireService;
    private SocieteRepository societeRepository;
    private DaRepository daRepository;
    private MembreAssuranceRepository membreAssuranceRepository;
    private Note_ConjonctureVIEService noteConjonctureVIEService;

    private NoteConjonctureCourtierTemporaireService noteConjonctureCourtierTemporaireService;
    private Note_ConjonctureCOURTIERService noteConjonctureCOURTIERService;
    private Note_ConjonctureIARDService noteConjonctureIARDService;
    private Note_ConjonctureIARD_temporaireService noteConjonctureIARDTemporaireService;

    // retrouver information par token
    @GetMapping("/withtoken")
    public ResponseEntity<Object> retrouverInfoparToken() {
        // Récupération de l'authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Aucune authentification trouvée.");
        }

        // Extraction de l'email depuis l'authentication
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof Da) {
            email = ((Da) principal).getEmail();
        }
        else if (principal instanceof MembreAssurance) {
            email = ((MembreAssurance) principal).getEmail();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le format d'authentification n'est pas reconnu.");
        }

        // Recherche de l'utilisateur dans différentes tables basées sur l'email
        return searchInTablesByEmail(email);
    }

    private ResponseEntity<Object> searchInTablesByEmail(String email) {
        Optional<Da> daOptional = Optional.ofNullable(daRepository.findByEmail(email));
        if (daOptional.isPresent()) {
            Da user = daOptional.get();
            InfoDTO userInfo = new InfoDTO(user.getNom(), user.getPrenoms(), user.getEmail(), user.getPoste(), user.getGenre(), user.getUsername(), user.getTelephone(), user.getRole().getLibelle());
            return ResponseEntity.ok(userInfo);
        }

        Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(email);
        if (membreAssuranceOptional.isPresent()) {
            MembreAssurance user = membreAssuranceOptional.get();
            InfoDTOMembreAssurance userInfo2 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(), user.getSociete().getTypeAssurance().getLibelles(), user.getTelephone());
            return ResponseEntity.ok(userInfo2);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun utilisateur trouvé pour l'e-mail: " + email);
    }

    // retrouver information avec email
    @PostMapping("/withEmail")
    public ResponseEntity<Object> retrouverInfoparEmail(@RequestBody Map<String, String> requestBody) {
        String email = requestBody.get("email");

        // Recherche dans la table Da
        Optional<Da> daOptional = Optional.ofNullable((daRepository.findByEmail(email)));
        if (daOptional.isPresent()) {
            Da user = daOptional.get();
            InfoDTO userInfo = new InfoDTO(user.getNom(),user.getPrenoms(), user.getEmail(),user.getPoste(), user.getGenre(), user.getUsername(),user.getTelephone(), user.getRole().getLibelle());
            return ResponseEntity.ok(userInfo);
        }

        // Recherche dans la table Societes
        Optional<Societes> societesOptional = societeRepository.findByEmail(email);
        if (societesOptional.isPresent()) {
            Societes user = societesOptional.get();
            infoDTO1 userInfo1 = new infoDTO1(user.getId_societe(),user.getNom(), user.getDirigeant(), user.getSiege(), user.getContact(), user.getNum_agrement(), user.getEmail(), user.getTypeAssurance().getLibelles(),user.getRoles().getLibelle());
            return ResponseEntity.ok(userInfo1);
        }

        // Recherche dans la table Membre Assurance
        Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(email);
        if (membreAssuranceOptional.isPresent()) {
            MembreAssurance user = membreAssuranceOptional.get();
            InfoDTOMembreAssurance userInfo2 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(),user.getSociete().getTypeAssurance().getLibelles(),user.getTelephone());
            return ResponseEntity.ok(userInfo2);
        }

        // Si aucun utilisateur n'est trouvé pour cet email
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun utilisateur trouvé pour l'e-mail: " + email);
    }



    @GetMapping("/rechercheParSocieteReelleIARD/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesIARD(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureIARD> notes =noteConjonctureIARDService.trouverInfoNote(nom,emailMembre,dateSoumission);
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
        }}

    @GetMapping("/rechercheParSocieteTemporaireIARD/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesEmailIARD(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureIARDTemporaire> notes =noteConjonctureIARDTemporaireService.trouverInfoNote(nom,emailMembre,dateSoumission);
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
    @GetMapping("/rechercheParSocieteReelleCOURTIER/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesCOURTIER(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureCourtier> notes =noteConjonctureCOURTIERService.trouverInfoNote(nom,emailMembre,dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureCOURTIER> dtoList = notes.stream()
                    .map(note -> new InfoDTOConjonctureCOURTIER(
                            note.getDateSoumission(),
                            note.getAnnee(),
                            note.getTrimestre(),
                            //infos general
                            note.getSociete(),
                            note.getSiege(),
                            note.getGerant(),
                            note.getTelephone(),
                            note.getEmail(),
                            note.getNumero_RCCM(),
                            //autres infos

                            note.getBureaux_secondaire(),
                            note.getAbidjan(),
                            note.getInterieur(),
                            note.getSociete_partenaire(),
                            note.getCompte_courant(),
                            note.getNombre_hommes(),
                            note.getNombre_femmes(),
                            note.getTotal_personnel(),
                            //activités de l'entreprise
                            note.getEmissions_primes(),
                            note.getCommissions_encaissees(),
                            note.getTotal_charge_ou_frais_generaux(),
                            note.getMontant_fonds_geres(),
                            note.getMontant_honoraires(),
                            note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                            note.getStatut().getEtat(),
                            note.getMembreAssurance().getNom(),
                            note.getMembreAssurance().getPrenoms(),
                            note.getMembreAssurance().getGenre(),
                            note.getMembreAssurance().getPoste(),
                            note.getMembreAssurance().getEmail()// Supposé calculé

                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }}

    @GetMapping("/rechercheParSocieteTemporaireCOURTIER/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesEmailCOURTIER(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureCourtierTemporaire> notes =noteConjonctureCourtierTemporaireService.trouverInfoNote(nom,emailMembre,dateSoumission);
        if (notes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<InfoDTOConjonctureCOURTIER> dtoList = notes.stream()
                    .map(note -> new InfoDTOConjonctureCOURTIER(
                            note.getDateSoumission(),
                            note.getAnnee(),
                            note.getTrimestre(),
                            //infos general
                            note.getSociete(),
                            note.getSiege(),
                            note.getGerant(),
                            note.getTelephone(),
                            note.getEmail(),
                            note.getNumero_RCCM(),
                            //autres infos

                            note.getBureaux_secondaire(),
                            note.getAbidjan(),
                            note.getInterieur(),
                            note.getSociete_partenaire(),
                            note.getCompte_courant(),
                            note.getNombre_hommes(),
                            note.getNombre_femmes(),
                            note.getTotal_personnel(),
                            //activités de l'entreprise
                            note.getEmissions_primes(),
                            note.getCommissions_encaissees(),
                            note.getTotal_charge_ou_frais_generaux(),
                            note.getMontant_fonds_geres(),
                            note.getMontant_honoraires(),
                            note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                            note.getStatut().getEtat(),
                            note.getMembreAssurance().getNom(),
                            note.getMembreAssurance().getPrenoms(),
                            note.getMembreAssurance().getGenre(),
                            note.getMembreAssurance().getPoste(),
                            note.getMembreAssurance().getEmail()// Supposé calculé

                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtoList);
        }
    }

    @GetMapping("/rechercheParSocieteReelleVIE/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesVIE(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureVIE> notes =noteConjonctureVIEService.trouverInfoNote(nom,emailMembre,dateSoumission);
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

    @GetMapping("/rechercheParSocieteTemporaireVIE/{nom}/{emailMembre}/{dateSoumission}")
    public ResponseEntity<?> rechercheParSocietesEmailVIE(
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<Note_ConjonctureVie_temporaire> notes = note_ConjonctureVIE_temporaireService.trouverInfoNote(nom, emailMembre,dateSoumission);
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
    }}
