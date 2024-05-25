package com.DA.DA.controller;


import com.DA.DA.dto.InfoDTOConjonctureCOURTIER;
import com.DA.DA.dto.InfoDTOConjonctureCOURTIERSUCCESS;
import com.DA.DA.dto.InfoDTOConjonctureVie;
import com.DA.DA.dto.infoDTOConjonture;
import com.DA.DA.entite.NoteConjonctureCourtier;
import com.DA.DA.entite.NoteConjonctureCourtierTemporaire;

import com.DA.DA.entite.NoteConjonctureIARDTemporaire;
import com.DA.DA.entite.Note_ConjonctureVie_temporaire;
import com.DA.DA.security.JwtServiceMembre;
import com.DA.DA.service.NoteConjonctureCourtierTemporaireService;
import com.DA.DA.service.Note_ConjonctureCOURTIERService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/COURTIER")
public class NoteConjonctureCourtierController {
    private NoteConjonctureCourtierTemporaireService noteConjonctureCourtierTemporaireService;
    private JwtServiceMembre jwtServiceMembre;
    private Note_ConjonctureCOURTIERService noteConjonctureCOURTIERService;

    @PostMapping("/creer")
    public ResponseEntity<?> createOrUpdateNoteConjonctureCOURTIERTemporaire(@RequestBody NoteConjonctureCourtierTemporaire note, HttpServletRequest request) {
        return noteConjonctureCourtierTemporaireService.createOrUpdateNoteConjonctureCOURTIERTemporaire(note, request);
    }



    @PostMapping("/final/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<?> creerFinal(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @RequestBody Map<String, Object> requestBody,
            HttpServletRequest request) {

        Long newStatutId = Long.parseLong(requestBody.get("newStatutId").toString());
        String contenuMessage = requestBody.containsKey("contenuMessage") ? requestBody.get("contenuMessage").toString() : null;

        return noteConjonctureCourtierTemporaireService.creerFinal(dateSoumission, nom, emailMembre, newStatutId, contenuMessage, request);
    }

    // afficher les notes de conjoncture dejà validé par un employé de la DA
    @GetMapping("/ConjonctureCOURTIEreelle")
    public ResponseEntity<List<InfoDTOConjonctureCOURTIERSUCCESS>> getAllConjonctureCOURTIERDTOsSUCCESS()  {
        List<InfoDTOConjonctureCOURTIERSUCCESS> infoDTOConjonctureCOURTIERSUCCESSES = noteConjonctureCourtierTemporaireService.getAllConjonctureCOURTIERDTOsSUCCESS();
        if ( infoDTOConjonctureCOURTIERSUCCESSES.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(infoDTOConjonctureCOURTIERSUCCESSES);
    }

    // Récupérer toutes les notes de la base de données temporaire ou l'admin ne peut que supprimer
    @GetMapping("/ConjonctureCOURTIERtemporaire")
    public ResponseEntity<List<InfoDTOConjonctureCOURTIER>> getAllConjonctureCOURTIERDTOs() {
        List<InfoDTOConjonctureCOURTIER> conjonctureCOURTIERDTOs = noteConjonctureCourtierTemporaireService.getAllConjonctureCOURTIERDTOs();
        if (conjonctureCOURTIERDTOs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(conjonctureCOURTIERDTOs);
    }
    @GetMapping("/rechercheNoteParNomSociete")
    public ResponseEntity<List<InfoDTOConjonctureCOURTIER>> trouverParNomCompagnie(HttpServletRequest request) {
        // Extraire le nom de la compagnie du token
        String token = request.getHeader("Authorization").substring(7); // Supprimer "Bearer "
        String nomCompagnie = jwtServiceMembre.lireNomCompagnieDuToken(token);

        // Vérifier si le nom de la compagnie est récupéré avec succès
        if (nomCompagnie == null || nomCompagnie.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Ou une autre réponse appropriée
        }

        List<NoteConjonctureCourtierTemporaire> notes = noteConjonctureCourtierTemporaireService.trouverNOTEParNomCompagnie(nomCompagnie);

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


    // afficher les notes de conjoncture dejà validé par un employé de la DA par compagnie
//    @GetMapping("/ConjonctureCOURTIERSUCCES/{nomCompagnie}")
//    public ResponseEntity<List<InfoDTOConjonctureCOURTIERSUCCESS>> getAllConjonctureCOURTIERDTOsSUCCESS1(String nomCompagnie)  {
//        List<InfoDTOConjonctureCOURTIERSUCCESS> infoDTOConjonctureCOURTIERSUCCESSES = noteConjonctureCourtierTemporaireService.getAllConjonctureCOURTIERDTOsSUCCESS1(nomCompagnie);
//        if ( infoDTOConjonctureCOURTIERSUCCESSES.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.ok(infoDTOConjonctureCOURTIERSUCCESSES);
//    }
    @GetMapping("/rechercheParDateSoumissionEmployee/{date}")
    public ResponseEntity<?>rechercheParDateSoumissionCompagnie (
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureCourtier> notes = noteConjonctureCOURTIERService.trouverParDateSoumission(dateSoumission);

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
    @GetMapping("/rechercheParSociete/{nom}")
    public ResponseEntity<?> rechercheParSocietes(
            @PathVariable("nom") String nom) {
        List<NoteConjonctureCourtier> notes =noteConjonctureCOURTIERService.trouverParNomsociete(nom);
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
    @GetMapping("/rechercheParDateSoumission/{date}")
    public ResponseEntity<?> rechercheParDateSoumissionSocieteEmploye(
            @PathVariable("date") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission) {
        List<NoteConjonctureCourtierTemporaire> notes = noteConjonctureCourtierTemporaireService.trouverParDateSoumission(dateSoumission);

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
    @GetMapping("/rechercheParSocieteEMPLOYE/{nom}")
    public ResponseEntity<?> rechercheParSociete(
            @PathVariable("nom") String nom) {
        List<NoteConjonctureCourtierTemporaire> notes =noteConjonctureCourtierTemporaireService.trouverParNomsociet(nom);
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

}
