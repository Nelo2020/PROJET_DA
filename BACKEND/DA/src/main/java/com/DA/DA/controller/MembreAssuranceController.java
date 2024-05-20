package com.DA.DA.controller;

import com.DA.DA.dto.InfoDTOMembreAssurance;
import com.DA.DA.dto.infoDTO1;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.service.MembreAssuranceService;
import com.DA.DA.service.SocieteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@Slf4j
@RequestMapping(value = "/api/MembreAssurance")

public class MembreAssuranceController {

    @Autowired
        private MembreAssuranceService membreAssuranceService;
    @Autowired
        private MembreAssuranceRepository membreAssuranceRepository;

    @PostMapping("/creer")
    public ResponseEntity<?> creerCompteCompagnie(@RequestBody MembreAssurance membreAssurance) {
        try {
            membreAssuranceService.creerCompteMembreAssurance(membreAssurance);
            return new ResponseEntity<>("Le compte compagnie a été créé avec succès.", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Une erreur est survenue lors de la création du compte compagnie.");
        }}


        @GetMapping("/membre")
        public ResponseEntity<List<InfoDTOMembreAssurance>> getMembre() {
            List<InfoDTOMembreAssurance> AdminUsers = membreAssuranceService.getMembre();
            return ResponseEntity.ok(AdminUsers);
        }
        @GetMapping("/membre/VIE")
        public ResponseEntity<List<InfoDTOMembreAssurance>> getUsersByVIE() {
           List<InfoDTOMembreAssurance> AdminUsers = membreAssuranceService.getUsersByVIE();
           return ResponseEntity.ok(AdminUsers);
        }

        @GetMapping("/membre/IARD")
        public ResponseEntity<List<InfoDTOMembreAssurance>> getUsersByIARD() {
           List<InfoDTOMembreAssurance> AdminUsers = membreAssuranceService.getUsersByIARD();
           return ResponseEntity.ok(AdminUsers);
        }
        @GetMapping("/membre/COURTIER")
        public ResponseEntity<List<InfoDTOMembreAssurance>> getUsersByCOURTIER() {
           List<InfoDTOMembreAssurance> AdminUsers = membreAssuranceService.getUsersByCOURTIER();
           return ResponseEntity.ok(AdminUsers);
    }
    }
