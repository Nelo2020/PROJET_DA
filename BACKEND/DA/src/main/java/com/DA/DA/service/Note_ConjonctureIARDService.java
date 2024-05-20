package com.DA.DA.service;


import com.DA.DA.dto.infoDTOConjonture;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.NoteConjonctureIARDTemporaire;
import com.DA.DA.entite.Note_ConjonctureIARD;
import com.DA.DA.entite.*;
import com.DA.DA.repository.*;
import com.DA.DA.security.JwtService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.DA.DA.entite.Type_AssuranceE.VIE;

@Service
public class Note_ConjonctureIARDService {
    @Autowired

    private DaRepository daRepository;
    private MembreAssuranceRepository membreAssuranceRepository;
    @Autowired
    private StatutRepository statutRepository;
    @Autowired
    private Note_ConjonctureIARDRepository noteConjonctureIARDRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private Note_ConjonctureIARD_temporaireRepository note_conjonctureIARD_temporaireRepository;
    @Autowired
    private MessagerieRepository messagerieRepository;



    public Note_ConjonctureIARDService(DaRepository daRepository, MembreAssuranceRepository membreAssuranceRepository, StatutRepository statutRepository, Note_ConjonctureIARDRepository noteConjonctureIARDRepository) {
        this.daRepository = daRepository;
        this.membreAssuranceRepository = membreAssuranceRepository;
        this.statutRepository = statutRepository;
        this.noteConjonctureIARDRepository = noteConjonctureIARDRepository;
    }

    private infoDTOConjonture convertToInfoDTOConjoncture(Note_ConjonctureIARD note) {
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
        List<Note_ConjonctureIARD> notes = noteConjonctureIARDRepository.findAll();
        return notes.stream()
                .map(this::convertToInfoDTOConjoncture)
                .collect(Collectors.toList());
    }


    public List<Note_ConjonctureIARD> trouverParDateSoumission(Date dateSoumission) {
        return noteConjonctureIARDRepository.findByDateSoumission(dateSoumission);
    }
    public List<Note_ConjonctureIARD> trouverParNomsociete(String nom) {
        return noteConjonctureIARDRepository.findByCompagnie(nom);
    }
    public List<Note_ConjonctureIARD> trouverInfoNote(String nom,String email,Date dateSoumission) {
        return noteConjonctureIARDRepository.findNoteConjoncture(nom,email,dateSoumission);
    }

    private void sendEmail(String from, String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public List<String> getAllEmails() {
        // Récupérer tous les emails des employés
        List<Da> allEmployees = (List<Da>) daRepository.findAll();

        // Créer une liste pour stocker les emails
        List<String> emails = new ArrayList<>();

        // Parcourir tous les employés et récupérer les emails
        for (Da employee : allEmployees) {
            // Vérifier si l'email est non null et non vide
            if (employee.getEmail() != null && !employee.getEmail().isEmpty()) {
                // Ajouter l'email à la liste
                emails.add(employee.getEmail());
            }
        }
        // Retourner la liste des emails
        return emails;
    }

    @Transactional
    public ResponseEntity<String> supprimerNC_Iard(Date dateSoumission, String nom, String emailMembre, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token JWT manquant ou mal formaté.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "

        // Extraire l'email de l'expéditeur du token JWT
        String emailExp = jwtService.extractEmailFromJwt(token);
        Long idEmploye = jwtService.extractIdFromJwt(token);

        // Récupérer la note de conjoncture dans la base temporaire
        Note_ConjonctureIARD note = noteConjonctureIARDRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);

        if (note != null) {
            // Supprimer la note conjoncture correspondante dans la vraie table
            noteConjonctureIARDRepository.deleteByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);

            // Récupérer la note de conjoncture dans la base temporaire
            NoteConjonctureIARDTemporaire noteTemporaire = note_conjonctureIARD_temporaireRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);
            if (noteTemporaire != null) {

                // Récupérer l'ID du membre en fonction de l'e-mail du membre
                Optional<MembreAssurance> membreAssuranceOptional = membreAssuranceRepository.findByEmail(emailMembre);
                if (!membreAssuranceOptional.isPresent()) {
                    return ResponseEntity.badRequest().body("Membre non trouvé pour l'e-mail spécifié.");
                }
                MembreAssurance membreAssurance = membreAssuranceOptional.get();
                Long idMembre = membreAssurance.getId_Membre();

                // Mettre à jour le statut de la note de conjoncture dans la base temporaire
                Statut newStatut = statutRepository.findById(3L)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Statut non disponible: 3"));
                noteTemporaire.setStatut(newStatut);

                // Créer un enregistrement de messagerie avec le motif indiquant que la note a été supprimée
                Messagerie messagerie = new Messagerie();
                messagerie.setMotif("Votre note de conjoncture soumise le " + dateSoumission + " a été supprimée.");
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
                    noteTemporaire.setMessagerie(idMessage.get());
                }
                // Sauvegarder la mise à jour de la note dans la base temporaire
                note_conjonctureIARD_temporaireRepository.save(noteTemporaire);
                messagerieRepository.save(messagerie);

                // Associer la messagerie à la note de conjoncture dans la base temporaire
                noteTemporaire.setMessagerie(messagerie);
                note_conjonctureIARD_temporaireRepository.save(noteTemporaire);
                // Récupérer tous les emails des employés
                List<String> emailsEmployes = getAllEmails();

                // Envoyer un message à tous les emails des employés
                String contenuEmailEmployes = "La note de conjoncture soumise par " + emailMembre + " le " + dateSoumission + " a été supprimée.";
                for (String email : emailsEmployes) {
                    sendEmail(emailMembre, email, "Suppression de la Note de Conjoncture", contenuEmailEmployes);
                }
            }
            return ResponseEntity.ok("La note de conjoncture a été supprimée avec succès.");
        }

        return ResponseEntity.badRequest().body("La note n'est pas trouvé.");
    }

}



