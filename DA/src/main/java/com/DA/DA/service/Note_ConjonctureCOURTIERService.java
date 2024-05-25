package com.DA.DA.service;


import com.DA.DA.dto.InfoDTOConjonctureCOURTIER;
import com.DA.DA.dto.InfoDTOConjonctureVie;
import com.DA.DA.entite.*;
import com.DA.DA.repository.*;

import com.DA.DA.security.JwtService;
import com.DA.DA.security.JwtServiceMembre;
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

@Service
public class Note_ConjonctureCOURTIERService {

    private  DaRepository daRepository;
    private JwtServiceMembre jwtServiceMembre;
    private  MembreAssuranceRepository membreAssuranceRepository;
    private  StatutRepository statutRepository;
    private  NoteConjonctureCourtierRepository noteConjonctureCourtierRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private NoteConjonctureCourtierTemporaireRepository noteConjonctureCourtierTemporaireRepository ;
    @Autowired
    private MessagerieRepository messagerieRepository;



    public Note_ConjonctureCOURTIERService(DaRepository daRepository, JwtServiceMembre jwtServiceMembre, MembreAssuranceRepository membreAssuranceRepository, StatutRepository statutRepository, NoteConjonctureCourtierRepository noteConjonctureCourtierRepository) {
        this.daRepository = daRepository;
        this.jwtServiceMembre = jwtServiceMembre;
        this.membreAssuranceRepository = membreAssuranceRepository;
        this.statutRepository = statutRepository;
        this.noteConjonctureCourtierRepository = noteConjonctureCourtierRepository;
    }

    public List<InfoDTOConjonctureCOURTIER> getAllConjonctureVieDTOs() {
        // Récupère toutes les notes de la base de données réelle (sans le suffixe _temporaire)
        List<NoteConjonctureCourtier> notes = noteConjonctureCourtierRepository.findAll();

        return notes.stream()
                .map(this::convertToInfoDTOConjonctureCOURTIER) // Assurez-vous que cette méthode accepte Note_ConjonctureVIE comme paramètre
                .collect(Collectors.toList());
    }
    private InfoDTOConjonctureCOURTIER convertToInfoDTOConjonctureCOURTIER(NoteConjonctureCourtier note) {
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

    public List<NoteConjonctureCourtier> trouverParDateSoumission(Date dateSoumission) {
        return noteConjonctureCourtierRepository.findByDateSoumission(dateSoumission);
    }
    public List<NoteConjonctureCourtier> trouverParNomsociete(String nom) {
        return noteConjonctureCourtierRepository.findByCompagnie(nom);
    }
    public List<NoteConjonctureCourtier> trouverInfoNote(String nom,String email,Date dateSoumission) {
        return noteConjonctureCourtierRepository.findNoteConjoncture(nom,email,dateSoumission);
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
    public ResponseEntity<String> supprimerNC_COURTIER(Date dateSoumission, String nom, String emailMembre, HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token JWT manquant ou mal formaté.");
        }
        String token = authorizationHeader.substring(7); // Supprimer "Bearer "

        // Extraire l'email de l'expéditeur du token JWT
        String emailExp = jwtService.extractEmailFromJwt(token);
        Long idEmploye = jwtService.extractIdFromJwt(token);

        // Récupérer la note de conjoncture dans la base temporaire
        NoteConjonctureCourtier note = noteConjonctureCourtierRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);

        if (note != null) {
            // Supprimer la note conjoncture correspondante dans la vraie table
            noteConjonctureCourtierRepository.deleteByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);

            // Récupérer la note de conjoncture dans la base temporaire
            NoteConjonctureCourtierTemporaire noteTemporaire = noteConjonctureCourtierTemporaireRepository.findByMembreAssuranceAndDateSoumission(nom, emailMembre, dateSoumission);
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
                noteConjonctureCourtierTemporaireRepository.save(noteTemporaire);
                messagerieRepository.save(messagerie);

                // Associer la messagerie à la note de conjoncture dans la base temporaire
                noteTemporaire.setMessagerie(messagerie);
                noteConjonctureCourtierTemporaireRepository.save(noteTemporaire);
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



