package com.DA.DA.service;

import com.DA.DA.dto.InfoDTO;
import com.DA.DA.dto.modificationDTO1;
import com.DA.DA.entite.*;
import com.DA.DA.repository.DaRepository;
import com.DA.DA.repository.MembreAssuranceRepository;
import com.DA.DA.repository.RoleRepository;
import com.DA.DA.repository.SocieteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService implements UserDetailsService {
    @Autowired
    private DaRepository daRepository;
    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;
    @Autowired
    private SocieteRepository societeRepository;
@Autowired
    private JavaMailSender mailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleRepository roleRepository;
    @Override

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche dans la base de données DA
        Optional<Da> daOptional = Optional.ofNullable(daRepository.findByEmail(email));
        if (daOptional.isPresent()) {
            return daOptional.get();  // Retourne l'utilisateur DA s'il est trouvé
        }

        // Recherche dans la base de données de Membre Assurance
        Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(email);
        if (membreAssuranceOptional.isPresent()) {
            return membreAssuranceOptional.get();  // Retourne l'utilisateur Membre Assurance s'il est trouvé
        }

        // Nouvelle vérification dans la table Societes
        Optional<Societes> societeOptional = societeRepository.findByEmail(email);
        if (societeOptional.isPresent()) {
            return (UserDetails) societeOptional.get();  // Retourne l'utilisateur Societe s'il est trouvé
        }

        // Si l'utilisateur n'est pas trouvé dans aucune des bases de données, lance une exception
        throw new UsernameNotFoundException("Aucun utilisateur trouvé avec l'email: " + email);
    }
    public List<InfoDTO> getUsersByRoleEmployee() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Da> allUsers = daRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTO> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Da user : allUsers) {
            if (user.getRole() != null && user.getRole().getLibelle() == TypeRoles.EMPLOYEE) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTO userInfo = new InfoDTO(user.getNom(),user.getPrenoms(), user.getEmail(),user.getPoste(),user.getGenre(),user.getUsername(), user.getTelephone(), user.getRole().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo);
            }
        }

        return employeeUsers;
    }

    public List<InfoDTO> getUsersByRoleAdmin() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "admin"
        Iterable<Da> allUsers = daRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTO> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Da user : allUsers) {
            if (user.getRole() != null && user.getRole().getLibelle() == TypeRoles.ADMINISTRATEUR) {
                // Créer un nouvel objet InfoDTO avec les champs requis
                InfoDTO userInfo = new InfoDTO(user.getNom(),user.getPrenoms(), user.getEmail(),user.getPoste(), user.getGenre(), user.getUsername(),user.getTelephone(), user.getRole().getLibelle());
                // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
                employeeUsers.add(userInfo);
            }
        }

        return employeeUsers;
    }


    public List<InfoDTO> getUsers() {
        // Récupérer tous les utilisateurs de la base de données avec le rôle "EMPLOYEE"
        Iterable<Da> allUsers = daRepository.findAll();

        // Initialiser une liste pour stocker les utilisateurs ayant le rôle "employé"
        List<InfoDTO> employeeUsers = new ArrayList<>();

        // Parcourir les utilisateurs et filtrer ceux ayant le rôle "employé"
        for (Da user : allUsers) {

            // Créer un nouvel objet InfoDTO avec les champs requis
            InfoDTO userInfo = new InfoDTO(user.getNom(),user.getPrenoms(), user.getEmail(),user.getPoste(), user.getGenre(), user.getUsername(),user.getTelephone(), user.getRole().getLibelle());
            // Ajouter l'utilisateur filtré à la liste des utilisateurs employés
            employeeUsers.add(userInfo);

        }

        return employeeUsers;
    }

    private void envoyerMessageModificationMotDePasseCompagnie(modificationDTO1 modificationDTO) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(" estherlune95@gmail.com");
            message.setTo(modificationDTO.email());
            message.setSubject("Modification de mot de passe");
            String nouveauMotDePasse = modificationDTO.nouveauMotDePasse();
            String mdp = nouveauMotDePasse;
            message.setText("Votre Mot de passe a été modifié  avec succes. votre nouveau mot de passe est : "+ mdp );
            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'e-mail");
        }

    }

    @Transactional
    public ResponseEntity<String> modifierCompte(modificationDTO1 modificationDTO) {
        String email = modificationDTO.email();
        String nouveauMotDePasse = modificationDTO.nouveauMotDePasse();
        String confirmerNouveauMotDePasse = modificationDTO.confirmerNouveauMotDePasse();
        Long roleId = (long) modificationDTO.role().getId(); // Supposition que Role dans modificationDTO contient un ID.

        // Vérification de l'existence de l'utilisateur avec l'e-mail spécifié
        Optional<Da> utilisateurOptional = Optional.ofNullable(daRepository.findByEmail(email));
        if (!utilisateurOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Da utilisateur = utilisateurOptional.get();

        // Vérification si le nouveau mot de passe correspond à la confirmation
        if (!nouveauMotDePasse.equals(confirmerNouveauMotDePasse)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le nouveau mot de passe et sa confirmation ne correspondent pas");
        }

        // Mise à jour du mot de passe si un nouveau mot de passe est fourni
        if (nouveauMotDePasse != null && !nouveauMotDePasse.isEmpty()) {
            String motPasseCrypte = passwordEncoder.encode(nouveauMotDePasse);
            utilisateur.setMotpasse(motPasseCrypte);
        }

        // Vérification et mise à jour du rôle de l'utilisateur
        if (roleId != null) {
            Optional<Role> roleOptional = roleRepository.findById(roleId);
            if (!roleOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Rôle spécifié invalide");
            }
            utilisateur.setRole(roleOptional.get());
        }

        // Enregistrer les modifications dans la base de données
        daRepository.save(utilisateur);

        // Envoi de l'e-mail de confirmation
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("estherlune95@gmail.com");
            message.setTo(email);
            message.setSubject("Modification de votre mot de passe");
            message.setText("Bonjour, Monsieur/ Madame " + utilisateur.getNom() + ", votre mot de passe a été modifié. Votre nouveau mot de passe est : " + nouveauMotDePasse);
            mailSender.send(message);
        } catch (Exception e) {
            // Gestion de l'erreur d'envoi d'e-mail
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'e-mail");
        }

        // Réponse de succès
        return ResponseEntity.ok("Le mot de passe de " + utilisateur.getNom() + " a été modifié avec succès");
    }

    public ResponseEntity<String> modifierMotDePasse(modificationDTO1 modificationDTO) {
        String email = modificationDTO.email();
        String nouveauMotDePasse = modificationDTO.nouveauMotDePasse();
        String confirmerNouveauMotDePasse = modificationDTO.confirmerNouveauMotDePasse();


        // Vérifier si les mots de passe sont nuls ou ne correspondent pas
        if (nouveauMotDePasse == null || confirmerNouveauMotDePasse == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le mot de passe ou sa confirmation ne peut pas être nul.");
        }

        if (!nouveauMotDePasse.equals(confirmerNouveauMotDePasse)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Les nouveaux mots de passe ne correspondent pas.");
        }


            // Vérifier dans la table MembreAssurance
            Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(email);
            if (membreAssuranceOptional.isPresent()) {
                MembreAssurance compagnie = membreAssuranceOptional.get();


                // Crypter le nouveau mot de passe

                String motPasseCrypte = passwordEncoder.encode(nouveauMotDePasse);
                // Modifier le mot de passe de la compagnie

                compagnie.setMotpasse(motPasseCrypte);
                membreAssuranceRepository.save(compagnie);

                // Envoi du message
                envoyerMessageModificationMotDePasseCompagnie(modificationDTO);
                return ResponseEntity.ok("Votre mot de passe a été modifié " );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aucun utilisateur ou compagnie trouvé avec l'email : " + email);
            }
        }




    public List<String> getAllEmails() {
        // Récupérer tous les Da de la base de données
        Iterable<Da> allDa = daRepository.findAll();

        // Créer une liste pour stocker les emails
        List<String> emails = new ArrayList<>();

        // Parcourir tous les Da et récupérer les emails
        for (Da da : allDa) {
            // Vérifier si l'email est non null et non vide
            if (da.getEmail() != null && !da.getEmail().isEmpty()) {
                // Ajouter l'email à la liste
                emails.add(da.getEmail());
            }
        }

        // Retourner la liste des emails
        return emails;
    }

}
