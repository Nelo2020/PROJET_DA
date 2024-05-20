package com.DA.DA.service;


import com.DA.DA.dto.infoDTOConjonture;
import com.DA.DA.entite.*;
import com.DA.DA.repository.*;
import com.DA.DA.security.JwtService;

import com.DA.DA.security.JwtServiceMembre;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
public class Note_ConjonctureIARD_temporaireService {
    private DaRepository daRepository;
    private HttpServletRequest request;

    private  EmployeeService employeeService;
    private JavaMailSender mailSender;
    private JwtService jwtService;
    private JwtServiceMembre jwtServiceMembre;
    private MembreAssuranceRepository membreAssuranceRepository;
    @Autowired
    private MembreAssuranceService membreAssuranceService;


    private MessagerieRepository messagerieRepository;
    private Note_ConjonctureIARDRepository noteConjonctureIARDRepository;
    private Note_ConjonctureIARD_temporaireRepository note_conjonctureIARD_temporaireRepository;

    private StatutRepository statutRepository;



    public NoteConjonctureIARDTemporaire createOrUpdateNoteConjonctureIARDTemporaire(NoteConjonctureIARDTemporaire note) {
        // Extraction du token JWT de l'en-tête Authorization
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Token JWT manquant ou mal formaté.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "

        // Vérification de la date de soumission actuelle
        Date dateSoumissionActuelle = new Date();
        List<NoteConjonctureIARDTemporaire> existingNotesByDate = note_conjonctureIARD_temporaireRepository.findByDateSoumission(dateSoumissionActuelle);

        if (!existingNotesByDate.isEmpty()) {
            // Si une note existe déjà pour la date de soumission actuelle
            // Vérifier si une note existe pour la société
            Long idSociete = jwtServiceMembre.lireIdSocieteDuToken(token);
            List<NoteConjonctureIARDTemporaire> existingNotesBySociete = note_conjonctureIARD_temporaireRepository.findBySocieteId(idSociete);

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
        NoteConjonctureIARDTemporaire savedNote = note_conjonctureIARD_temporaireRepository.save(note);

        // Récupérer tous les emails de la table DA
        List<String> emails = employeeService.getAllEmails();

        // Construire le contenu du message à envoyer
        // Envoyer un e-mail à chaque email de la table DA
        String subject = "Nouvelle Note de Conjoncture";
        String content = "Bonjour, une nouvelle note de conjoncture IARD a été créée par l'utilisateur " + membreAssurance.getNom() + " de la compagnie " + note.getMembreAssurance().getSociete().getNom() +
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
    private infoDTOConjonture convertToInfoDTOConjoncture(NoteConjonctureIARDTemporaire note) {
        return new infoDTOConjonture(
                note.getDateSoumission(),
                note.getAnnee(),
                note.getTrimestre(),
                note.getNombre_de_contrats_assurance(),
                note.getPrime_emise_nette_annulations(),
                note.getMaladie_prime_emise(),
                note.getAccidents_corporels_prime_emise(),
                note.getRc_automobile_prime_emise(),
                note.getAutres_risques_automobiles_prime_emise(),
                note.getIncendie_et_autres_dommages_aux_biens_prime_emise(),
                note.getCredit_prime_emise(),
                note.getCaution_prime_emise(),
                note.getAssurance_importation_des_marchandises_prime_emise(),
                note.getMicroassurance_prime_emise(),
                note.getAssurances_indicielles_prime_emise(),
                note.getAutres_risques_dommages_prime_emise(),
                note.getAcceptations_dommages(),
                note.getPrime_cedee_en_reassurance(),
                note.getSinistre_a_payees(),
                note.getStock_de_sinistres_bons_a_payer(),
                note.getMaladie_stock(),
                note.getAccidents_corporels_stock(),
                note.getRc_automobile_stock(),
                note.getAutres_risques_automobiles_stock(),
                note.getIncendie_et_autres_dommages_aux_biens_stock(),
                note.getCredit_stock(),
                note.getCaution_stock(),
                note.getAssurance_importation_des_marchandises_stock(),
                note.getMicroassurance_stock(),
                note.getAssurances_indicielles_stock(),
                note.getAutres_risques_dommages_stock(),
                note.getDont_sisnistre_payee(),
                note.getRecours_encaisser(),
                note.getRecours_effectivement_encaisser(),
                note.getProduction_des_courtiers(),
                note.getCommissions_servies_aux_courtiers(),
                note.getProduction_autres_intermediaires(),
                note.getCommissions_servies_aux_autres_intermediaires(),
                note.getCreances_sur_etat(),
                note.getPrime_impayee(),
                note.getEmprunts_public_echus(),
                note.getFrais_du_personnel(),
                note.getNombre_hommes(),
                note.getNombre_femmes(),
                note.getEffectif_du_personnel(),
                note.getAutres_charges(),
                note.getMembreAssurance().getSociete().getNom(),
                note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                note.getStatut().getEtat(),
                note.getMembreAssurance().getNom(),
                note.getMembreAssurance().getPrenoms(),
                note.getMembreAssurance().getGenre(),
                note.getMembreAssurance().getPoste(),
                note.getMembreAssurance().getEmail()

        );
    }
    public List<infoDTOConjonture> getAllConjonctureIARDDTOs() {
        List<NoteConjonctureIARDTemporaire> notes = note_conjonctureIARD_temporaireRepository.findAll();
        return notes.stream()
                .map(this::convertToInfoDTOConjoncture)
                .collect(Collectors.toList());
    }

    public List<NoteConjonctureIARDTemporaire> trouverParDateSoumission(Date dateSoumission) {
        return note_conjonctureIARD_temporaireRepository.findByDateSoumission(dateSoumission);
    }

    public List<NoteConjonctureIARDTemporaire> trouverParNomsociet(String nom) {
        return note_conjonctureIARD_temporaireRepository.findByCompagnie(nom);
    }
    public List<NoteConjonctureIARDTemporaire> trouverParNomCompagnie(String nomCompagnie) {
        return note_conjonctureIARD_temporaireRepository.findByCompagnie(nomCompagnie);


    }
    public List<NoteConjonctureIARDTemporaire> trouverInfoNote(String nom,String email,Date dateSoumission) {
        return  note_conjonctureIARD_temporaireRepository.findNoteConjoncture(nom,email,dateSoumission);
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

        // Vérifier si le token est expiré
        if (jwtService.isTokenExpired(token)) {
            return ResponseEntity.badRequest().body("Votre token a expiré.");
        }
        // Extraire l'ID de l'employé et l'e-mail de l'expéditeur du token JWT
        Long idEmploye = jwtService.extractIdFromJwt(token);
        String emailExp = jwtService.extractEmailFromJwt(token);

        // Récupérer la note de conjoncture en fonction du nom de la société, de la date de soumission et de l'e-mail du membre de la compagnie d'assurance
        NoteConjonctureIARDTemporaire note = note_conjonctureIARD_temporaireRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);
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
            note_conjonctureIARD_temporaireRepository.save(note);

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
            Note_ConjonctureIARD newNote = new Note_ConjonctureIARD();
            BeanUtils.copyProperties(note, newNote, "id", "statut");
            newNote.setDateSoumission(note.getDateSoumission());
            newNote.setAnnee(note.getAnnee());
            newNote.setTrimestre(note.getTrimestre());
            newNote.setNombre_de_contrats_assurance(note.getNombre_de_contrats_assurance());
            newNote.setPrime_emise_nette_annulations(note.getPrime_emise_nette_annulations());
            newNote.setMaladie_prime_emise(note.getMaladie_prime_emise());
            newNote.setAccidents_corporels_prime_emise(note.getAccidents_corporels_prime_emise());
            newNote.setRc_automobile_prime_emise(note.getRc_automobile_prime_emise());
            newNote.setAutres_risques_automobiles_prime_emise(note.getAutres_risques_automobiles_prime_emise());
            newNote.setIncendie_et_autres_dommages_aux_biens_prime_emise(note.getIncendie_et_autres_dommages_aux_biens_prime_emise());
            newNote.setCredit_prime_emise(note.getCredit_prime_emise());
            newNote.setCaution_prime_emise(note.getCaution_prime_emise());
            newNote.setAssurance_importation_des_marchandises_prime_emise(note.getAssurance_importation_des_marchandises_prime_emise());
            newNote.setMicroassurance_prime_emise(note.getMicroassurance_prime_emise());
            newNote.setAssurances_indicielles_prime_emise(note.getAssurances_indicielles_prime_emise());
            newNote.setAutres_risques_dommages_prime_emise(note.getAutres_risques_dommages_prime_emise());
            newNote.setAcceptations_dommages(note.getAcceptations_dommages());
            newNote.setPrime_cedee_en_reassurance(note.getPrime_cedee_en_reassurance());
            newNote.setSinistre_a_payees(note.getSinistre_a_payees());
            newNote.setStock_de_sinistres_bons_a_payer(note.getStock_de_sinistres_bons_a_payer());
            newNote.setMaladie_stock(note.getMaladie_stock());
            newNote.setAccidents_corporels_stock(note.getAccidents_corporels_stock());
            newNote.setRc_automobile_stock(note.getRc_automobile_stock());
            newNote.setAutres_risques_automobiles_stock(note.getAutres_risques_automobiles_stock());
            newNote.setIncendie_et_autres_dommages_aux_biens_stock(note.getIncendie_et_autres_dommages_aux_biens_stock());
            newNote.setCredit_stock(note.getCredit_stock());
            newNote.setCaution_stock(note.getCaution_stock());
            newNote.setAssurance_importation_des_marchandises_stock(note.getAssurance_importation_des_marchandises_stock());
            newNote.setMicroassurance_stock(note.getMicroassurance_stock());
            newNote.setAssurances_indicielles_stock(note.getAssurances_indicielles_stock());
            newNote.setAutres_risques_automobiles_stock(note.getAutres_risques_automobiles_stock());
            newNote.setDont_sisnistre_payee(note.getDont_sisnistre_payee());
            newNote.setRecours_encaisser(note.getRecours_encaisser());
            newNote.setRecours_effectivement_encaisser(note.getRecours_effectivement_encaisser());
            newNote.setProduction_des_courtiers(note.getProduction_des_courtiers());
            newNote.setCommissions_servies_aux_courtiers(note.getCommissions_servies_aux_courtiers());
            newNote.setProduction_autres_intermediaires(note.getProduction_autres_intermediaires());
            newNote.setCommissions_servies_aux_autres_intermediaires(note.getCommissions_servies_aux_autres_intermediaires());
            newNote.setCreances_sur_etat(note.getCreances_sur_etat());
            newNote.setPrime_impayee(note.getPrime_impayee());
            newNote.setEmprunts_public_echus(note.getEmprunts_public_echus());
            newNote.setFrais_du_personnel(note.getFrais_du_personnel());
            newNote.setNombre_hommes( note.getNombre_hommes());
            newNote.setNombre_femmes(note.getNombre_femmes());
            newNote.setEffectif_du_personnel(note.getEffectif_du_personnel());
            newNote.setAutres_charges(note.getAutres_charges());
            newNote.setStatut(newStatut);

            noteConjonctureIARDRepository.save(newNote);

            // Sauvegarder la note avec le nouveau statut dans notevietemporaire
            note_conjonctureIARD_temporaireRepository.save(note);

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
