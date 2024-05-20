package com.DA.DA.service;


import com.DA.DA.dto.InfoDTOMembreAssurance;
import com.DA.DA.entite.MembreAssurance;

import com.DA.DA.entite.Da;

import com.DA.DA.entite.Societes;
import com.DA.DA.repository.DaRepository;

import com.DA.DA.repository.MembreAssuranceRepository;

import com.DA.DA.repository.SocieteRepository;
import com.DA.DA.repository.Type_assuranceRepository;
import com.DA.DA.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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


@AllArgsConstructor
@Transactional
@Service
public class MembreAssuranceService implements UserDetailsService {

@Autowired
    private JwtService jwtService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private DaRepository daRepository;
    @Autowired
    private  BCryptPasswordEncoder passwordEncoder;
    private Type_assuranceRepository typeAssuranceRepository;
    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;

    @Autowired
    private SocieteRepository societeRepository;



    public void creerCompteMembreAssurance(@RequestBody MembreAssurance membreAssurance) {
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
        membreAssurance.setDa(da);

//       Societes societes = societeRepository.findById(societeId)
//                .orElseThrow(() -> new IllegalArgumentException("Employé (DA) non trouvé."));
//        membreAssurance.setSociete(societes);

        // Vérifier si l'e-mail est valide
        if (!isValidEmail(membreAssurance.getEmail())) {
            throw new IllegalArgumentException("L'e-mail de la compagnie est invalide.");
        }

        // Vérifier si l'e-mail est déjà utilisé
        Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(membreAssurance.getEmail());
        if (membreAssuranceOptional.isPresent()) {
            throw new RuntimeException("Votre e-mail est déjà utilisé.");
        }
        Societes societes = membreAssurance.getSociete();
        Long societeId =  societes.getId_societe();
        societes= societeRepository.findById(societeId).orElse(null);
        if (societes == null) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La societe spécifiée n'existe pas");
        }
        // Vérifier si le rôle est correct (id != 3)


        // Vérifier si le type d'assurance est valide
//        Type_Assurance typeAssurance = typeAssuranceRepository.findById((long) membreAssurance.getType_assurance().getId())
//                .orElseThrow(() -> new IllegalArgumentException("Type d'assurance non valide."));
//        compagnie.setType_assurance(typeAssurance);
        String mdp = membreAssurance.getMotpasse();

        // Crypter le mot de passe avant de l'enregistrer
        membreAssurance.setMotpasse(passwordEncoder.encode(membreAssurance.getMotpasse()));

        // Enregistrer le compte compagnie
       membreAssuranceRepository.save(membreAssurance);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("estherlune95@gmail.com");
            message.setTo(membreAssurance.getEmail());
            message.setSubject("Création de Compte Compagnie");
            message.setText("Bonjour, Monsieur/Madame " + membreAssurance.getNom() + ", votre compte a été créé avec succès pour le mail: "+ membreAssurance.getEmail() + " et mot de passe " + mdp);
            mailSender.send(message);
        } catch (Exception e) {
            // Gestion de l'erreur d'envoi d'e-mail
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'e-mail");
        }
        // Réponse de succès
        ResponseEntity.ok("Bonjour Monsieur/Madame " + membreAssurance.getNom() +"de la societe"+ membreAssurance.getSociete().getNom()+ " votre compte a été créé avec succès" + "avec l'email" + membreAssurance.getEmail() + "et le mot de passe " + membreAssurance.getMotpasse());
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


    public List<InfoDTOMembreAssurance> getMembre() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<MembreAssurance> allUsers = membreAssuranceRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTOMembreAssurance> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (MembreAssurance user : allUsers) {
            {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTOMembreAssurance userInfo1 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(),user.getSociete().getTypeAssurance().getLibelles(),user.getTelephone());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }

    @Override

    public UserDetails loadUserByUsername(String email)  throws UsernameNotFoundException {
        return this.membreAssuranceRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Aucun utilisateur ne correspond à cet identifiant"));
    }
    public Optional<MembreAssurance> findByEmail(String email) {
        return membreAssuranceRepository.findByEmail(email);
    }

    public List<InfoDTOMembreAssurance> getUsersByVIE() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<MembreAssurance> allUsers = membreAssuranceRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTOMembreAssurance> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (MembreAssurance user : allUsers) {
            if (user.getSociete().getTypeAssurance().getLibelles().equals(VIE)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTOMembreAssurance userInfo1 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(),user.getSociete().getTypeAssurance().getLibelles(),user.getTelephone());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }
    public List<InfoDTOMembreAssurance> getUsersByIARD() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<MembreAssurance> allUsers = membreAssuranceRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTOMembreAssurance> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (MembreAssurance user : allUsers) {
            if (user.getSociete().getTypeAssurance().getLibelles().equals(IARD)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTOMembreAssurance userInfo1 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(),user.getSociete().getTypeAssurance().getLibelles(),user.getTelephone());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }
    public List<InfoDTOMembreAssurance> getUsersByCOURTIER() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<MembreAssurance> allUsers = membreAssuranceRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTOMembreAssurance> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (MembreAssurance user : allUsers) {
            if (user.getSociete().getTypeAssurance().getLibelles().equals(COURTIER)) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTOMembreAssurance userInfo1 = new InfoDTOMembreAssurance(user.getNom(), user.getPrenoms(), user.getGenre(), user.getEmail(), user.getUsername(), user.getPoste(), user.getSociete().getNom(),user.getSociete().getTypeAssurance().getLibelles(),user.getTelephone());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo1);
            }
        }

        return employeeUsers;
    }

}



