package com.DA.DA.service;


import com.DA.DA.dto.InfoDTOConjonctureCOURTIER;
import com.DA.DA.dto.InfoDTOConjonctureCOURTIERSUCCESS;
import com.DA.DA.entite.*;
import com.DA.DA.repository.*;
import com.DA.DA.security.JwtService;

import com.DA.DA.security.JwtServiceMembre;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@NoArgsConstructor
@Service
public class NoteConjonctureCourtierTemporaireService {

    @Autowired
    private DaRepository daRepository;

    @Autowired
  private JwtServiceMembre jwtServiceMembre;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private MembreAssuranceService membreAssuranceService;


    @Autowired
    private MessagerieRepository messagerieRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Note_ConjonctureVIERepository noteConjonctureVIERepository;

    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;

    @Autowired
    private StatutRepository statutRepository;

    @Autowired
    private NoteConjonctureCourtierTemporaireRepository noteConjonctureCourtierTemporaireRepository;

    @Autowired
    private NoteConjonctureCourtierRepository noteConjonctureCourtierRepository;

    public ResponseEntity<?> createOrUpdateNoteConjonctureCOURTIERTemporaire(NoteConjonctureCourtierTemporaire note, HttpServletRequest request) {
        try {
            // Extraction du token JWT de l'en-tête Authorization
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token JWT manquant ou mal formaté.");
            }
            String token = authorizationHeader.substring(7); // Supprimer "Bearer "
            Long idDa = jwtServiceMembre.lireIdDaDuToken(token);
            Long idCompagnie = jwtServiceMembre.lireIdMembreDuToken(token);

            // Vérification de l'existence d'une note pour la date de soumission actuelle
            Date dateSoumissionActuelle = new Date();
            List<NoteConjonctureCourtierTemporaire> existingNotesByDate = noteConjonctureCourtierTemporaireRepository.findByDateSoumission(dateSoumissionActuelle);
            if (!existingNotesByDate.isEmpty()) {
                // Si une note existe déjà pour la date de soumission actuelle
                // Vérifier si l'ID membre existe dans la table des notes
                List<NoteConjonctureCourtierTemporaire> existingNotesByMembre = noteConjonctureCourtierTemporaireRepository.findByMembreAssuranceId(idCompagnie);
                if (!existingNotesByMembre.isEmpty()) {
                    throw new IllegalArgumentException("Vous avez déjà soumis une note de conjoncture aujourd'hui.");
                }

                // Récupération des entités DA et Membre_Assurance à partir du token
                Da da = daRepository.findById(idDa)
                        .orElseThrow(() -> new IllegalStateException("DA non trouvé"));
                MembreAssurance membreAssurance = membreAssuranceRepository.findById(idCompagnie)
                        .orElseThrow(() -> new IllegalStateException("Membre non trouvé"));

                // Récupérer l'ID de la société associée au membre
                Long idSociete = membreAssurance.getSociete().getId_societe();

                // Vérification de l'existence de l'ID de la société dans la table des notes
                List<NoteConjonctureCourtierTemporaire> existingNotesBySociete = noteConjonctureCourtierTemporaireRepository.findBySocieteId(idSociete);
                if (!existingNotesBySociete.isEmpty()) {
                    throw new IllegalArgumentException("Une note de conjoncture a déjà été soumise aujourd'hui pour cette société.");
                }

                // Si l'ID membre n'existe pas dans la table des notes
                // et que l'ID de la société n'est pas associé à une autre note
                // Créer la note et envoyer l'e-mail
                return createNoteAndSendEmail(note, da, idCompagnie, dateSoumissionActuelle);
            } else {
                // Si aucune note existe pour la date de soumission actuelle, créer la note et envoyer l'e-mail
                Da da = daRepository.findById(idDa)
                        .orElseThrow(() -> new IllegalStateException("DA non trouvé"));

                // Créer la note et envoyer l'e-mail
                return createNoteAndSendEmail(note, da, idCompagnie, dateSoumissionActuelle);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> createNoteAndSendEmail(NoteConjonctureCourtierTemporaire note, Da da, Long idMembre, Date dateSoumission) {
        // Ajout automatique de la date de soumission
        note.setDateSoumission(dateSoumission);

        // Associer les entités extraites à la note
        note.setDa(da);
        MembreAssurance membreAssurance = membreAssuranceRepository.findById(idMembre)
                .orElseThrow(() -> new IllegalStateException("Membre non trouvé"));
        note.setMembreAssurance(membreAssurance);

        // Sauvegarder la note dans la base de données temporaire
        NoteConjonctureCourtierTemporaire savedNote = noteConjonctureCourtierTemporaireRepository.save(note);

        // Récupérer tous les emails de la table DA
        List<String> emails = employeeService.getAllEmails();

        // Construire le contenu du message à envoyer
        // Envoyer un e-mail à chaque email de la table DA
        String subject = "Nouvelle Note de Conjoncture";
        String content = "Bonjour, une nouvelle note de conjoncture a été créée par l'utilisateur " + membreAssurance.getNom() + " de la compagnie " + note.getMembreAssurance().getSociete().getNom() +
                " le " + note.getDateSoumission() + ".";

        for (String email : emails) {
            sendEmail(email, subject, content);
        }

        return new ResponseEntity<>(savedNote, HttpStatus.CREATED);
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
    private InfoDTOConjonctureCOURTIER convertToInfoDTOConjonctureCOURTIER_TEMPORAIRE(NoteConjonctureCourtierTemporaire note) {
        return new InfoDTOConjonctureCOURTIER(
                note.getDateSoumission(),
                note.getAnnee(),
                note.getTrimestre(),

                //infos general
                note.getSociete(),
                note.getSiege(),
                note.getGerant(),
                note.getTelephone(),
                note.getEmail(),
                note.getNumero_RCCM(),
                //autres infos

                note.getBureaux_secondaire(),
                note.getAbidjan(),
                note.getInterieur(),
                note.getSociete_partenaire(),
                note.getCompte_courant(),
                note.getNombre_hommes(),
                note.getNombre_femmes(),
                note.getTotal_personnel(),
                //activités de l'entreprise
                note.getEmissions_primes(),
                note.getCommissions_encaissees(),
                note.getTotal_charge_ou_frais_generaux(),
                note.getMontant_fonds_geres(),
                note.getMontant_honoraires(),
                note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                note.getStatut().getEtat(),
                note.getMembreAssurance().getNom(),
                note.getMembreAssurance().getPrenoms(),
                note.getMembreAssurance().getGenre(),
                note.getMembreAssurance().getPoste(),
                note.getMembreAssurance().getEmail()// Supposé calculé
        );
    }
    public List<InfoDTOConjonctureCOURTIER> getAllConjonctureCOURTIERDTOs() {
        List<NoteConjonctureCourtierTemporaire> notes = noteConjonctureCourtierTemporaireRepository.findAll();
        return notes.stream()
                .map(this::convertToInfoDTOConjonctureCOURTIER_TEMPORAIRE)
                .collect(Collectors.toList());
    }
    public List<NoteConjonctureCourtierTemporaire> trouverNOTEParNomCompagnie(String nomCompagnie) {
        return noteConjonctureCourtierTemporaireRepository.findByCompagnie(nomCompagnie);
    }

    public List<NoteConjonctureCourtierTemporaire> trouverInfoNote(String nom,String email,Date dateSoumission) {
        return noteConjonctureCourtierTemporaireRepository.findNoteConjoncture(nom,email,dateSoumission);
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
        NoteConjonctureCourtierTemporaire note = noteConjonctureCourtierTemporaireRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);
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
            noteConjonctureCourtierTemporaireRepository.save(note);

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

            // Copier la note dans noteconjonctureCourtier
            NoteConjonctureCourtier newNote = new NoteConjonctureCourtier();
            BeanUtils.copyProperties(note, newNote, "id", "statut");
            newNote.setDateSoumission(note.getDateSoumission());
            newNote.setAnnee(note.getAnnee());
            newNote.setTrimestre(note.getTrimestre());
            newNote.setSociete(note.getSociete());
            newNote.setSiege(note.getSiege());
            newNote.setGerant(note.getGerant());
            newNote.setTelephone(note.getTelephone());
            newNote.setEmail(note.getEmail());
            newNote.setNumero_RCCM(note.getNumero_RCCM());
            newNote.setBureaux_secondaire(note.getBureaux_secondaire());
            newNote.setAbidjan(note.getAbidjan());
            newNote.setInterieur(note.getInterieur());
            newNote.setSociete_partenaire(note.getSociete_partenaire());
            newNote.setCompte_courant(note.getCompte_courant());
            newNote.setNombre_hommes(note.getNombre_hommes());
            newNote.setNombre_femmes(note.getNombre_femmes());
            newNote.setTotal_personnel(note.getTotal_personnel());
            newNote.setEmissions_primes(note.getEmissions_primes());
            newNote.setCommissions_encaissees(note.getCommissions_encaissees());
            newNote.setTotal_charge_ou_frais_generaux(note.getTotal_charge_ou_frais_generaux());
            newNote.setMontant_fonds_geres(note.getMontant_fonds_geres());
            newNote.setMontant_honoraires(note.getMontant_honoraires());
            newNote.setStatut(newStatut);

            // Sauvegarder la nouvelle note
            noteConjonctureCourtierRepository.save(newNote);

            // Sauvegarder la note avec le nouveau statut dans notevietemporaire
            noteConjonctureCourtierTemporaireRepository.save(note);

            // Envoyer un e-mail de notification
            String sujetEmail = "Validation de la Note de Conjoncture";
            sendEmail(emailExp, emailMembre, sujetEmail, contenuMessage);

            // Renvoyer une réponse
            return ResponseEntity.ok().body("Note de conjoncture validée et copiée avec succès.");
        }

        // Si le nouveau statut n'est ni "Rejeté" ni "Validé", renvoyer une réponse d'erreur
        return ResponseEntity.badRequest().body("Le nouveau statut spécifié n'est pas valide.");
    }


    private InfoDTOConjonctureCOURTIERSUCCESS convertToInfoDTOConjonctureCOURTIER(NoteConjonctureCourtier note) {
        return new InfoDTOConjonctureCOURTIERSUCCESS(
                note.getDateSoumission(),
                note.getAnnee(),
                note.getTrimestre(),

                //infos general
                note.getSociete(),
                note.getSiege(),
                note.getGerant(),
                note.getTelephone(),
                note.getEmail(),
                note.getNumero_RCCM(),
                //autres infos

                note.getBureaux_secondaire(),
                note.getAbidjan(),
                note.getInterieur(),
                note.getSociete_partenaire(),
                note.getCompte_courant(),
                note.getNombre_hommes(),
                note.getNombre_femmes(),
                note.getTotal_personnel(),
                //activités de l'entreprise
                note.getEmissions_primes(),
                note.getCommissions_encaissees(),
                note.getTotal_charge_ou_frais_generaux(),
                note.getMontant_fonds_geres(),
                note.getMontant_honoraires(),
                note.getMembreAssurance().getSociete().getTypeAssurance().getLibelles(),
                note.getStatut().getEtat(),
                note.getMembreAssurance().getNom(),
                note.getMembreAssurance().getPrenoms(),
                note.getMembreAssurance().getGenre(),
                note.getMembreAssurance().getPoste(),
                note.getMembreAssurance().getEmail()
                // Supposé calculé
        );
    }
    public List<NoteConjonctureCourtierTemporaire> trouverParDateSoumission(Date dateSoumission) {
        return noteConjonctureCourtierTemporaireRepository.findByDateSoumission(dateSoumission);
    }

    public List<NoteConjonctureCourtierTemporaire> trouverParNomsociet(String nom) {
        return noteConjonctureCourtierTemporaireRepository.findByCompagnie(nom);
    }
    public List<InfoDTOConjonctureCOURTIERSUCCESS> getAllConjonctureCOURTIERDTOsSUCCESS() {
        List<NoteConjonctureCourtier> notes = noteConjonctureCourtierRepository.findAll();
        return notes.stream()
                .map(this::convertToInfoDTOConjonctureCOURTIER)
                .collect(Collectors.toList());
    }

    public List<InfoDTOConjonctureCOURTIERSUCCESS> getAllConjonctureCOURTIERDTOsSUCCESS1(String compagnie) {
        List<NoteConjonctureCourtier> notes = noteConjonctureCourtierRepository.findByCompagnie(compagnie);
        return notes.stream()
                .map(this::convertToInfoDTOConjonctureCOURTIER)
                .collect(Collectors.toList());
    }
    private void sendEmail(String from, String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

}
