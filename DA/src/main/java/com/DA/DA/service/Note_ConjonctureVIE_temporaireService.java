package com.DA.DA.service;

import com.DA.DA.dto.InfoDTOConjonctureVie;
import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Note_ConjonctureVie_temporaire;
import com.DA.DA.entite.*;
import com.DA.DA.repository.*;
import com.DA.DA.security.JwtService;

import com.DA.DA.security.JwtServiceMembre;
import jakarta.mail.Multipart;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class Note_ConjonctureVIE_temporaireService {
    private DaRepository daRepository;
    private  HttpServletRequest request;
    private JwtServiceMembre jwtServiceMembre;
    private EmployeeService employeeService;
    private JwtService jwtService;
    private MembreAssuranceService membreAssuranceService;
    private MessagerieRepository messagerieRepository;
    private JavaMailSender mailSender;
    private Note_ConjonctureVIERepository noteConjonctureVIERepository;
    private MembreAssuranceRepository membreAssuranceRepository;
    private StatutRepository statutRepository;
    private Note_ConjonctureVIE_temporaireRepository note_conjonctureVIE_temporaireRepository;



    public Note_ConjonctureVie_temporaire createOrUpdateNoteConjonctureVIETemporaire(Note_ConjonctureVie_temporaire note) {
        // Extraction du token JWT de l'en-tête Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token JWT manquant ou mal formaté.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "

        // Vérification de la date de soumission actuelle
        Date dateSoumissionActuelle = new Date();
        List<Note_ConjonctureVie_temporaire> existingNotesByDate = note_conjonctureVIE_temporaireRepository.findByDateSoumission(dateSoumissionActuelle);

        if (!existingNotesByDate.isEmpty()) {
            // Si une note existe déjà pour la date de soumission actuelle
            // Vérifier si une note existe pour la société
            Long idSociete = jwtServiceMembre.lireIdSocieteDuToken(token);
            List<Note_ConjonctureVie_temporaire> existingNotesBySociete = note_conjonctureVIE_temporaireRepository.findBySocieteId(idSociete);

            if (!existingNotesBySociete.isEmpty()) {
                throw new IllegalArgumentException("Votre société a déjà soumis une note de conjoncture aujourd'hui, veuillez attendre demain svp.");
            }
        }

        // Récupération des entités DA et Membre_Assurance à partir du token
        Long idDa = jwtServiceMembre.lireIdDaDuToken(token);
        Da da = daRepository.findById(idDa)
                .orElseThrow(() -> new IllegalStateException("DA non trouvé"));

        Long idMembre = jwtServiceMembre.lireIdMembreDuToken(token);
        MembreAssurance membreAssurance = membreAssuranceRepository.findById(idMembre)
                .orElseThrow(() -> new IllegalStateException("Membre non trouvé"));

        // Ajout automatique de la date de soumission
        note.setDateSoumission(dateSoumissionActuelle);

        // Associer les entités extraites à la note
        note.setDa(da);
        note.setMembreAssurance(membreAssurance);

        // Sauvegarder la note dans la base de données temporaire
        Note_ConjonctureVie_temporaire savedNote = note_conjonctureVIE_temporaireRepository.save(note);

        // Récupérer tous les emails de la table DA
        List<String> emails = employeeService.getAllEmails();

        // Construire le contenu du message à envoyer
        // Envoyer un e-mail à chaque email de la table DA
        String subject = "Nouvelle Note de Conjoncture";
        String content = "Bonjour, une nouvelle note de conjoncture Vie a été créée par l'utilisateur " + membreAssurance.getNom() + " de la compagnie " + note.getMembreAssurance().getSociete().getNom() +
                " le " + note.getDateSoumission() + ".";
        for (String email : emails) {
            sendEmail(email, subject, content);
        }

        return savedNote;
    }


    // Méthode pour envoyer un e-mail
    private void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("estherlune95@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public List<InfoDTOConjonctureVie> getAllConjonctureVieDTOs() {
        List<Note_ConjonctureVie_temporaire> notes = note_conjonctureVIE_temporaireRepository.findAll();
        return notes.stream()
                .map(this::convertToInfoDTOConjonctureVie_temporaire)
                .collect(Collectors.toList());
    }
    private InfoDTOConjonctureVie convertToInfoDTOConjonctureVie_temporaire(Note_ConjonctureVie_temporaire note) {
        return new InfoDTOConjonctureVie(
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
                note.getMembreAssurance().getEmail()// Supposé calculé
        );
    }

    public List<Note_ConjonctureVie_temporaire> trouverParDateSoumission(Date dateSoumission) {
        return note_conjonctureVIE_temporaireRepository.findByDateSoumission(dateSoumission);
    }
    public List<Note_ConjonctureVie_temporaire> trouverParNomsociet(String nom) {
        return note_conjonctureVIE_temporaireRepository.findByCompagnie(nom);
    }
    public List<Note_ConjonctureVie_temporaire> trouverParNomCompagnie(String nomCompagnie) {
        return note_conjonctureVIE_temporaireRepository.findByCompagnie(nomCompagnie);
    }

    public List<Note_ConjonctureVie_temporaire> trouverInfoNote(String nom,String email,Date dateSoumission) {
        return  note_conjonctureVIE_temporaireRepository.findNoteConjoncture(nom,email,dateSoumission);
    }
    public ResponseEntity<?> creerFinal(
            @PathVariable("dateSoumission") Date dateSoumission,
            @PathVariable("nom") String nom,
            @PathVariable("emailMembre") String emailMembre,
            @RequestBody Long newStatutId,
            @RequestBody(required = false) String contenuMessage,
            HttpServletRequest request) {

        // Vérification de l'existence et de la validité du jeton JWT dans l'en-tête de la requête
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token JWT manquant ou mal formaté.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "

        // Extraire l'ID de l'employé et l'e-mail de l'expéditeur du token JWT
        Long idEmploye = jwtService.extractIdFromJwt(token);
        String emailExp = jwtService.extractEmailFromJwt(token);

        // Récupérer la note de conjoncture en fonction du nom de la société, de la date de soumission et de l'e-mail du membre de la compagnie d'assurance
        Note_ConjonctureVie_temporaire note = note_conjonctureVIE_temporaireRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);
        if (note == null) {
            return ResponseEntity.badRequest().body("Note de conjoncture non trouvée pour la date de soumission et le nom de compagnie spécifiés.");
        }
        // Créer une nouvelle instance de la messagerie
        Messagerie messagerie = new Messagerie();
        // Récupérer l'ID du membre en fonction de l'e-mail du membre
        Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(emailMembre);
        if (!membreAssuranceOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Membre non trouvé pour l'e-mail spécifié.");
        }
        MembreAssurance membreAssurance = membreAssuranceOptional.get();
        Long idMembre = membreAssurance.getId_Membre();

        // Vérifier si le statut actuel de la note est déjà "Validé" ou "Rejeté"
        if (note.getStatut().getIdStatut() == 2) { // Si le statut est "Validé"
            return ResponseEntity.badRequest().body("Le statut de cette note ne peut plus être modifié car elle est déjà stockée dans la vraie table.");
        } else if (note.getStatut().getIdStatut() == 3) { // Si le statut est "Rejeté"
            return ResponseEntity.badRequest().body("Le statut de cette note est déjà 'Rejeté',vous devez renvoyer une nouvelle note");
        }

        // Récupérer le statut correspondant à l'ID spécifié
        Statut newStatut = statutRepository.findById(newStatutId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Statut non disponible: " + newStatutId));

        // Mettre à jour le statut de la note de conjoncture avec le nouveau statut
        note.setStatut(newStatut);

        // Si le nouveau statut est "Rejeté" (ID = 3), stocker le contenu du message dans la table de la messagerie
        if (newStatutId == 3) {
            if (contenuMessage == null || contenuMessage.isEmpty()) {
                return ResponseEntity.badRequest().body("Le contenu du message est obligatoire lorsque le statut est 'Rejeté'.");
            }

            messagerie.setMotif(contenuMessage);

            // Récupérer l'objet Da correspondant à l'ID de l'employé
            Da daEmploye = daRepository.findById(idEmploye)
                    .orElseThrow(() -> new EntityNotFoundException("Da introuvable avec l'ID: " + idEmploye));
            messagerie.setDa(daEmploye);

            messagerie.setMembreAssurance(membreAssurance);
            messagerieRepository.save(messagerie);
            Long messageId = messagerie.getId(); // Assurez-vous que getId() retourne l'ID de la messagerie

            // Récupérer la messagerie associée à l'ID
            Optional<Messagerie> idMessage = messagerieRepository.findById(messageId);

            // Vérifier si la messagerie existe
            if (idMessage.isPresent()) {
                // Mettre à jour le champ id_message de la note dans notevietemporaire
                note.setMessagerie(idMessage.get());
            }

            // Mettre à jour l'ID du statut de la note dans notevietemporaire en 3
            note_conjonctureVIE_temporaireRepository.save(note);

            // Envoyer un e-mail à la compagnie
            sendEmail(emailExp, emailMembre, "Sujet de l'e-mail", contenuMessage);

            // Renvoyer une réponse
            return ResponseEntity.ok().body("Message envoyé avec succès, note de conjoncture rejetée.");
        } else if (newStatutId == 2) {
            // Si le nouveau statut est "Validé" (ID = 2), stocker un contenu de message par défaut
            String contenuDefaut = "Votre note de conjoncture a été validée.";
            if (contenuMessage == null || contenuMessage.isEmpty()) {
                contenuMessage = contenuDefaut;
            }


            // Copier la note dans noteconjonctureVie
           Note_ConjonctureVIE newNote = new Note_ConjonctureVIE();
           BeanUtils.copyProperties(note, newNote, "id", "statut");
            // Utiliser les données de l'entité note pour initialiser la nouvelle note

            newNote.setDateSoumission(note.getDateSoumission());
            newNote.setAnnee(note.getAnnee());newNote.setTrimestre(note.getTrimestre());
            newNote.setNombre_contrat_assurance(note.getNombre_contrat_assurance());
           newNote.setPrime_emise_nette_annulations(note.getPrime_emise_nette_annulations());
           newNote.setAssurance_individuelle_prime(note.getAssurance_individuelle_prime());
           newNote.setContrats_cas_vie_prime_emise(note.getContrats_cas_vie_prime_emise());
           newNote.setContrats_cas_deces_prime_emise(note.getContrats_cas_deces_prime_emise());
           newNote.setContrats_mixtes_prime_emise( note.getContrats_mixtes_prime_emise());
           newNote.setCapitalisation_et_epargne_prime_emise(note.getCapitalisation_et_epargne_prime_emise());
           newNote.setComplementaire_prime_emise(note.getComplementaire_prime_emise());
           newNote.setAssurance_collective_prime(note.getAssurance_collective_prime());
           newNote.setContrats_cas_vie_collective_prime_emise(note.getContrats_cas_vie_collective_prime_emise());
           newNote.setContrats_cas_deces_collective_prime_emise(note.getContrats_cas_deces_collective_prime_emise());
           newNote.setContrats_mixtes_collective_prime_emise(note.getContrats_mixtes_collective_prime_emise());
           newNote.setCapitalisation_et_epargne_collective_prime_emise(note.getCapitalisation_et_epargne_collective_prime_emise());
           newNote.setComplementaire_collective_prime_emise(note.getComplementaire_collective_prime_emise());
           newNote.setMicroassurance_vie_prime_emise(note.getMicroassurance_vie_prime_emise());
           newNote.setAcceptations_vie(note.getAcceptations_vie());
           newNote.setPrimes_cedees_en_reassurance(note.getPrimes_cedees_en_reassurance());
           newNote.setProvisions_mathematiques(note.getProvisions_mathematiques());
           newNote.setStock_de_sinistres_et_capitaux_echus(note.getStock_de_sinistres_et_capitaux_echus());
           newNote.setPrestations_vie_effectivement_payees(note.getPrestations_vie_effectivement_payees());
           newNote.setAssurances_individuelles(note.getAssurances_individuelles());
           newNote.setContrats_en_cas_de_vie_individuelles_prestation_vie(note.getContrats_en_cas_de_vie_individuelles_prestation_vie());
           newNote.setContrats_en_cas_de_deces_individuelles_prestation_vie(note.getContrats_en_cas_de_deces_individuelles_prestation_vie());
           newNote.setContrats_mixtes_individuelles_prestation_vie(note.getContrats_mixtes_individuelles_prestation_vie());
           newNote.setCapitalisation_et_epargne_individuelles_prestation_vie(note.getCapitalisation_et_epargne_individuelles_prestation_vie());
           newNote.setComplementaires_individuelles_prestation_vie(note.getComplementaires_individuelles_prestation_vie());
           newNote.setAssurances_collectives(note.getAssurances_collectives());
           newNote.setContrats_en_cas_de_vie_collectives_prestation_vie(note.getContrats_en_cas_de_vie_collectives_prestation_vie());newNote.setContrats_en_cas_de_deces_collectives_prestation_vie(note.getContrats_en_cas_de_deces_collectives_prestation_vie());
           newNote.setContrats_mixtes_collectives_prestation_vie(note.getContrats_mixtes_collectives_prestation_vie());
           newNote.setCapitalisation_et_epargne_collectives_prestation_vie(note.getCapitalisation_et_epargne_collectives_prestation_vie());
           newNote.setComplementaires_collectives_prestation_vie(note.getComplementaires_collectives_prestation_vie());
           newNote.setMicroassurance_vie_prestation_vie(note.getMicroassurance_vie_prestation_vie());
           newNote.setProduction_des_courtiers(note.getProduction_des_courtiers());
           newNote.setCommissions_servies_courtiers(note.getCommissions_servies_courtiers());
           newNote.setProduction_des_autres_intermediaires(note.getProduction_des_autres_intermediaires());
           newNote.setCommissions_servies_autres_intermediaires(note.getCommissions_servies_autres_intermediaires());
           newNote.setAutres_charges(note.getAutres_charges());
           newNote.setDont_frais_du_personnel(note.getDont_frais_du_personnel());
           newNote.setNombre_hommes( note.getNombre_hommes());
           newNote.setNombre_femmes(note.getNombre_femmes());
           newNote.setEffectif_du_personnel(note.getEffectif_du_personnel());
           newNote.setCreances_sur_etat(note.getCreances_sur_etat());
           newNote.setPrime_impayee(note.getPrime_impayee());
           newNote.setEmprunts_publics_echus(note.getEmprunts_publics_echus());
           newNote.setStatut(newStatut);


            noteConjonctureVIERepository.save(newNote);

            // Sauvegarder la note avec le nouveau statut dans notevietemporaire
            note_conjonctureVIE_temporaireRepository.save(note);

            // Envoyer un e-mail de notification
            String sujetEmail = "Validation de la Note de Conjoncture";
            sendEmail(emailExp, emailMembre, sujetEmail, contenuMessage);

            // Renvoyer une réponse
            return ResponseEntity.ok().body("Note de conjoncture validée et copiée avec succès.");
        }

        // Si le nouveau statut n'est ni "Rejeté" ni "Validé", renvoyer une réponse d'erreur
        return ResponseEntity.badRequest().body("Le nouveau statut spécifié n'est pas valide.");
    }
    // Méthode pour envoyer un e-mail
    private void sendEmail(String from, String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

}
