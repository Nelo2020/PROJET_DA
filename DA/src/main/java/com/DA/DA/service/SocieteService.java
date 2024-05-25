package com.DA.DA.service;

import com.DA.DA.dto.infoDTO1;
import com.DA.DA.dto.modificationSocieteDTO;
import com.DA.DA.entite.*;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.repository.Type_assuranceRepository;
import com.DA.DA.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.DA.DA.entite.Type_AssuranceE.*;

@Service
public class SocieteService implements UserDetailsService {
@Autowired
    private JavaMailSender mailSender;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private DaRepository daRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private Type_assuranceRepository typeAssuranceRepository;
    @Autowired
    private SocieteRepository societeRepository;
    public void creerCompteSociete(@RequestBody Societes societes) {
        // Extraction du token JWT de l'en-tête Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Aucun token JWT fourni ou format invalide.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "
        Long daId = jwtService.extractIdFromJwt(token); // Extrait l'ID de DA

        // Récupérer et associer l'employé (DA) à la compagnie
        Da da = daRepository.findById(daId)
                .orElseThrow(() -> new IllegalArgumentException("Employé (DA) non trouvé."));
        societes.setDa(da);

        // Vérifier si l'e-mail est valide
        if (!isValidEmail(societes.getEmail())) {
            throw new IllegalArgumentException("L'e-mail de la compagnie est invalide.");
        }

        // Vérifier si l'e-mail est déjà utilisé
        Optional<Societes> societesOptional = societeRepository.findByEmail(societes.getEmail());
        if (societesOptional.isPresent()) {
            throw new RuntimeException("Votre e-mail est déjà utilisé.");
        }

        // Vérifier si le rôle est correct (id != 3)
        if (societes.getRoles().getId() != 3) {
            throw new IllegalArgumentException("L'option 3 est réservée aux compagnies.");
        }

        // Vérifier si le type d'assurance est valide
        Type_Assurance typeAssurance = typeAssuranceRepository.findById((long) societes.getTypeAssurance().getId_typeassurance())
                .orElseThrow(() -> new IllegalArgumentException("Type d'assurance non valide."));
        societes.setTypeAssurance(typeAssurance);

        // Crypter le mot de passe avant de l'enregistrer


        // Enregistrer le compte compagnie
      societeRepository.save(societes);


        // Réponse de succès
        ResponseEntity.ok("Le compte de " + societes.getNom() + "du siege " + societes.getSiege()+" a été créé avec succès");
    }



    private boolean isValidEmail(String email) {
        // Vérifier si l'e-mail est null ou vide
        if (email == null || email.isEmpty()) {
            return false;
        }

        // Vérifier si l'e-mail contient un @ et au moins un point après le @
        if (!email.contains("@") || !email.contains(".")) {
            return false;
        }

        return true; // Si toutes les conditions sont satisfaites, l'e-mail est considéré comme valide
    }
    public List<infoDTO1> getUsersByVIE() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Societes> allUsers = societeRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<infoDTO1> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Societes user : allUsers) {
            if (user.getTypeAssurance().getLibelles().equals(VIE)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
            infoDTO1 userInfo1 = new infoDTO1(user.getId_societe(),user.getNom(), user.getDirigeant(), user.getSiege(), user.getContact(), user.getNum_agrement(), user.getEmail(), user.getTypeAssurance().getLibelles(),user.getRoles().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }
    public List<infoDTO1> getUsersByIARD() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Societes> allUsers = societeRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<infoDTO1> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Societes user : allUsers) {
            if (user.getTypeAssurance().getLibelles().equals(IARD)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                infoDTO1 userInfo1 = new infoDTO1(user.getId_societe(),user.getNom(), user.getDirigeant(), user.getSiege(), user.getContact(), user.getNum_agrement(), user.getEmail(), user.getTypeAssurance().getLibelles(),user.getRoles().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }
    public List<infoDTO1> getUsersByCOURTIER() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Societes> allUsers = societeRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<infoDTO1> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Societes user : allUsers) {
            if (user.getTypeAssurance().getLibelles().equals(COURTIER)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                infoDTO1 userInfo1 = new infoDTO1(user.getId_societe(),user.getNom(), user.getDirigeant(), user.getSiege(), user.getContact(), user.getNum_agrement(), user.getEmail(), user.getTypeAssurance().getLibelles(),user.getRoles().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }
    public List<infoDTO1> getSociete() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Societes> allUsers = societeRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<infoDTO1> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Societes user : allUsers) {
            {
                // Créer un nouvel objet InfoDTO avec les champs requis
                infoDTO1 userInfo1 = new infoDTO1(user.getId_societe(),user.getNom(), user.getDirigeant(), user.getSiege(), user.getContact(), user.getNum_agrement(), user.getEmail(), user.getTypeAssurance().getLibelles(),user.getRoles().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }

    @Override

    public UserDetails loadUserByUsername(String email)  throws UsernameNotFoundException {
        return (UserDetails) this.societeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet identifiant"));
    }
    public Optional<Societes> findByEmail(String email) {
        return societeRepository.findByEmail(email);
    }
    public Optional<Societes> findByNom(String nomCompagnie) {
        return societeRepository.findByNom(nomCompagnie);
    }

    public ResponseEntity<String> modifier(modificationSocieteDTO modificationDTO) {
        String nom = modificationDTO.nom();
        String dirigeant = modificationDTO.dirigeant();
        String siege = modificationDTO.siege();
        String contact = modificationDTO.contact();
        String num_agrement = modificationDTO.num_agrement();
        String email = modificationDTO.email();


        // Vérification de l'existence de l'utilisateur avec l'e-mail spécifié
        Optional<Societes> utilisateurOptional = societeRepository.findByEmail(email);
        if (!utilisateurOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("societé non trouvé");
        }
        Societes utilisateur = utilisateurOptional.get();
        utilisateur.setNom(nom);
        utilisateur.setDirigeant(dirigeant);
        utilisateur.setSiege(siege);
        utilisateur.setContact(contact);
        utilisateur.setNum_agrement(num_agrement);
        utilisateur.setEmail(email);


        // Enregistrer les modifications dans la base de données
        societeRepository.save(utilisateur);

        // Réponse de succès
        return ResponseEntity.ok("La societe " + utilisateur.getNom() + " a été modifié avec succès");

    }
}
