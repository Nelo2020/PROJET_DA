package com.DA.DA.controller;

import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import com.DA.DA.entite.TypeRoles;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.service.Note_ConjonctureCOURTIERService;
import com.DA.DA.service.Note_ConjonctureIARDService;
import com.DA.DA.service.Note_ConjonctureVIEService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/api/SUPPRESSION")
public class SuppressionController {

    @Autowired
    DaRepository daRepository;
    @Autowired
    SocieteRepository societeRepository;
    @Autowired
    MembreAssuranceRepository membreAssuranceRepository;

    @Autowired
    Note_ConjonctureIARDService  noteConjonctureIARDService;
    @Autowired
    Note_ConjonctureVIEService  noteConjonctureVIEService;
    @Autowired
    Note_ConjonctureCOURTIERService noteConjonctureCOURTIERService;

    @DeleteMapping("/supprimer")
    public ResponseEntity<String> supprimerCompteParEmail(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("L'email est manquant dans le corps de la requête.");
            }

            // Tentative de suppression pour un utilisateur Da
            Optional<Da> utilisateurOptional = Optional.ofNullable(daRepository.findByEmail(email));
            if (utilisateurOptional.isPresent()) {
                Da utilisateur = utilisateurOptional.get();
                if (utilisateur.getRole().getLibelle() == TypeRoles.EMPLOYEE) {
                    daRepository.delete(utilisateur);
                    return ResponseEntity.ok("Compte Da supprimé avec succès.");
                } else {
                    return ResponseEntity.badRequest().body("Impossible de supprimer le compte Da car son rôle n'est pas 'EMPLOYEE'.");
                }
            }

            // Tentative de suppression pour une Societe
            Optional<Societes> societeOptional = societeRepository.findByEmail(email);
            if (societeOptional.isPresent()) {
                Societes societe = societeOptional.get();
                societeRepository.delete(societe);
                return ResponseEntity.ok("Compte Societe supprimé avec succès.");
            }

            // Tentative de suppression pour un MembreAssurance
            Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(email);
            if (membreAssuranceOptional.isPresent()) {
                MembreAssurance membreAssurance = membreAssuranceOptional.get();
                membreAssuranceRepository.delete(membreAssurance);
                return ResponseEntity.ok("Compte MembreAssurance supprimé avec succès.");
            }

            return ResponseEntity.badRequest().body("Aucun utilisateur, société ou membre d'assurance trouvé avec cet email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de la suppression du compte : " + e.getMessage());
        }
    }
    //Méthode suppression de la note de conjoncture IARD
    @DeleteMapping("supprimerIARD/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<String> supprimerNC_Iard(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            HttpServletRequest request) {
        ResponseEntity<String> response = noteConjonctureIARDService.supprimerNC_Iard(dateSoumission, nom, emailMembre,request);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.ok("La note de conjoncture a été supprimée avec succès.");
        } else {
            return ResponseEntity.badRequest().body("La note n'a pas été trouvée.");
        }
    }

    //Méthode suppression de la note de conjoncture vie
    @DeleteMapping("/supprimerVIE/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<String> supprimerNC_VIE(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            HttpServletRequest request) {
        ResponseEntity<String> response = noteConjonctureVIEService.supprimerNC_VIE(dateSoumission, nom, emailMembre, request);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.ok("La note de conjoncture a été supprimée avec succès.");
        } else {
            return ResponseEntity.badRequest().body("La note n'a pas été trouvée.");
        }}
//Methode de suppression de la note de conjoncture courtier
    @DeleteMapping("supprimerCOURTIER/{dateSoumission}/{nom}/{emailMembre}")
    public ResponseEntity<String> supprimerNC_COURTIER(
            @PathVariable("dateSoumission") @DateTimeFormat(pattern="yyyy-MM-dd") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            HttpServletRequest request) {
        ResponseEntity<String> response = noteConjonctureCOURTIERService.supprimerNC_COURTIER(dateSoumission, nom, emailMembre,request);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return ResponseEntity.ok("La note de conjoncture a été supprimée avec succès.");
        } else {
            return ResponseEntity.badRequest().body("La note n'a pas été trouvée.");
        }
    }

}
