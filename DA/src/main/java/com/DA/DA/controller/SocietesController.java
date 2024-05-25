package com.DA.DA.controller;



import com.DA.DA.dto.infoDTO1;
import com.DA.DA.entite.Da;
import com.DA.DA.entite.Societes;
import com.DA.DA.entite.TypeRoles;

import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.service.SocieteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/api/Societe")

public class SocietesController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
private SocieteRepository societeRepository;
    @Autowired

 private SocieteService societeService;



    @PostMapping("/creer")
    public ResponseEntity<?> creerCompteCompagnie(@RequestBody Societes societes) {
        try {
            societeService.creerCompteSociete(societes);
            return new ResponseEntity<>("Le compte compagnie a été créé avec succès.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Une erreur est survenue lors de la création du compte compagnie.");
        }
    }
    @GetMapping("/societe/VIE")
    public ResponseEntity<List<infoDTO1>> getUsersByVIE() {
        List<infoDTO1> AdminUsers = societeService.getUsersByVIE();
        return ResponseEntity.ok(AdminUsers);
    }

    @GetMapping("/societe/IARD")
    public ResponseEntity<List<infoDTO1>> getUsersByIARD() {
        List<infoDTO1> AdminUsers = societeService.getUsersByIARD();
        return ResponseEntity.ok(AdminUsers);
    }
    @GetMapping("/societe/COURTIER")
    public ResponseEntity<List<infoDTO1>> getUsersByCOURTIER() {
        List<infoDTO1> AdminUsers = societeService.getUsersByCOURTIER();
        return ResponseEntity.ok(AdminUsers);
    }

    @GetMapping("/societe")
    public ResponseEntity<List<infoDTO1>> getCompagnie() {
        List<infoDTO1> AdminUsers = societeService.getSociete();
        return ResponseEntity.ok(AdminUsers);
    }
}