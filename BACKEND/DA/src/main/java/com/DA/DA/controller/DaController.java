package com.DA.DA.controller;

//import com.DA.DA.DTO.AuthenticationDTO;
//import com.DA.DA.DTO.InfoDTO;
//import com.DA.DA.DTO.infoDTO1;
//

import com.DA.DA.dto.InfoDTO;
import com.DA.DA.entite.Da;
import com.DA.DA.entite.Role;
import com.DA.DA.entite.TypeRoles;

import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.RoleRepository;

import com.DA.DA.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Validation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.DA.DA.entite.TypeRoles.ADMINISTRATEUR;
import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;
//import com.DA.DA.entites.Validation;
//import com.DA.DA.repository.Compagnie_AssuranceRepository;
//import com.DA.DA.security.JwtService;
//import com.DA.DA.security.JwtServiceCompagnie;
//import com.DA.DA.service.Compagnie_AssuranceService;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/DA")
public class DaController {
    private JavaMailSender mailSender;
    final  DaRepository daRepository;
    final RoleRepository roleRepository;
    final PasswordEncoder passwordEncoder;
    final AuthenticationManager authenticationManager;

    final EmployeeService employeeService;



    @PostMapping("/creer")
    public ResponseEntity<String> createDaAccount(@RequestBody Da da) {
        // Vérification des champs obligatoires
        // Vérification de l'e-mail
        if (!isValidEmail(da.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'e-mail n'est pas valide");
        }
        Optional<Da> daoptional = Optional.ofNullable(this.daRepository.findByEmail(da.getEmail()));
        if (daoptional.isPresent()) {
            throw new RuntimeException("votre mail est déjà utilisé");
        }
        // Vérification de l'existence du rôle
        Role role = da.getRole();
        if (role == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le rôle spécifié n'existe pas");
        }
        if(da.getRole().getId()==3){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le rôle spécifié n'est pas attribué aux elements de la DA");
        }
        Long roleId = (long) role.getId();
        role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le rôle spécifié n'existe pas");
        }
        // Vérification du mot de passe
        String motPasse = da.getMotpasse();
        if (motPasse == null || motPasse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le mot de passe ne peut pas être vide");
        }
        // Encodage du mot de passe
        String mdp= da.getMotpasse();
        String encodedPassword = passwordEncoder.encode(motPasse);
        // Affecter le mot de passe encodé à l'utilisateur

        da.setMotpasse(encodedPassword);
        // Enregistrement dans la base de données
        daRepository.save(da);
        // Envoi de l'e-mail
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("estherlune95@gmail.com");
            message.setTo(da.getEmail());
            message.setSubject("Création de Compte employé Da");
            message.setText("Bonjour, Monsieur/Madame " + da.getNom() + ", votre compte a été créé avec succès pour le mail: "+ da.getEmail() + " et mot de passe " + mdp);
            mailSender.send(message);
        } catch (Exception e) {
            // Gestion de l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'e-mail");
        }
        // Réponse de succès
        return ResponseEntity.ok("Le compte de " + da.getNom() + " a été créé avec succès");
    }

    // Méthode pour valider l'e-mail
    private boolean isValidEmail(String email) {
        // Vérifie si l'e-mail contient un @, un point et se termine par @tresor.gouv.ci
        return email != null && email.contains("@") && email.contains(".") ;
    }

    @GetMapping("/users/EMPLOYEE")
    public ResponseEntity<List<InfoDTO>> getUsersByRoleEmployee() {
        List<InfoDTO> employeeUsers = employeeService.getUsersByRoleEmployee();
        return ResponseEntity.ok(employeeUsers);
    }


    @GetMapping("/users/ADMINISTRATEUR")
    public ResponseEntity<List<InfoDTO>> getUsersByRoleAdmin() {
        List<InfoDTO> AdminUsers = employeeService.getUsersByRoleAdmin();
        return ResponseEntity.ok(AdminUsers);
    }


    @GetMapping("/users")
    public ResponseEntity<List<InfoDTO>> getUsers() {
        List<InfoDTO> AdminUsers = employeeService.getUsers();
        return ResponseEntity.ok(AdminUsers);
    }
}