package com.DA.DA.controller;

import com.DA.DA.dto.AuthenticationDTO;
import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.security.JwtService;

import com.DA.DA.security.JwtServiceMembre;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/CONNEXION_COMPTES")
public class ConnexionController {
    @Autowired
  private  DaRepository daRepository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private  JwtService jwtService;
    @Autowired
   private JwtServiceMembre jwtServiceMembre;
    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;
    @Autowired
    private SocieteRepository societeRepository;



        @PostMapping("/connexion")
        public ResponseEntity<?> authenticateUser(@RequestBody AuthenticationDTO authenticationDTO) {
            // Vérifier dans la table Da
            Optional<Da> userOptional = Optional.ofNullable(daRepository.findByEmail(authenticationDTO.email()));
            if (userOptional.isPresent()) {
                Da user = userOptional.get();
                if (passwordEncoder.matches(authenticationDTO.motpasse(), user.getMotpasse())) {
                    return generateResponse(user);
                }
            }

            // Vérifier dans la table MembreAssurance
            Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(authenticationDTO.email());
            if (membreAssuranceOptional.isPresent()) {
                MembreAssurance membreAssurance = membreAssuranceOptional.get();
                if (passwordEncoder.matches(authenticationDTO.motpasse(), membreAssurance.getMotpasse())) {
                    return generateResponseMembre(membreAssurance);
                }
            }

            // Aucun utilisateur trouvé
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects");
        }

        private ResponseEntity<?> generateResponse(Da user) {
            String token = jwtService.generate(user.getEmail()).get("bearer");
            if (jwtService.isTokenExpired(token)) {
                token = jwtService.generate(user.getEmail()).get("bearer");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
//            response.put("nom", user.getNom());
//            response.put("prenoms", user.getPrenoms());
//            response.put("telephone", user.getTelephone());
//            response.put("role", user.getRole().getLibelle());
//            response.put("email", user.getEmail());
//            response.put("poste", user.getPoste());
//            response.put("genre", user.getGenre());

            return ResponseEntity.ok(response);
        }

        private ResponseEntity<?> generateResponseMembre(MembreAssurance membreAssurance) {
            String token = jwtServiceMembre.generate(membreAssurance.getEmail()).get("bearer");
            if (jwtServiceMembre.isTokenExpired(token)) {
                token = jwtServiceMembre.generate(membreAssurance.getEmail()).get("bearer");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
//            response.put("nom", membreAssurance.getNom());
//            response.put("prenoms", membreAssurance.getPrenoms());
//            response.put("email", membreAssurance.getEmail());
//            response.put("telephone", membreAssurance.getTelephone());
//            response.put("poste", membreAssurance.getPoste());
//            response.put("genre", membreAssurance.getGenre());
//            response.put("type_assurance", membreAssurance.getSociete().getTypeAssurance().getLibelles());
            return ResponseEntity.ok(response);
        }
    }