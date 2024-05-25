package com.DA.DA.repository;
import com.DA.DA.entite.NoteConjonctureIARDTemporaire;
import com.DA.DA.entite.Note_ConjonctureVie_temporaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface Note_ConjonctureVIE_temporaireRepository extends JpaRepository<Note_ConjonctureVie_temporaire, Long> {




    List<Note_ConjonctureVie_temporaire> findByDateSoumission(Date dateSoumission);
    @Query("SELECT nc FROM Note_ConjonctureVie_temporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    Note_ConjonctureVie_temporaire findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                                         @Param("email") String emailMembre,
                                                                         @Param("dateSoumission") Date dateSoumission);

    @Query("SELECT nc FROM Note_ConjonctureVie_temporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<Note_ConjonctureVie_temporaire> findByCompagnie(@Param("nom") String nom);



    @Query("SELECT nc FROM Note_ConjonctureVie_temporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<Note_ConjonctureVie_temporaire> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);

    @Query("SELECT nc FROM Note_ConjonctureVie_temporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<Note_ConjonctureVie_temporaire> findNoteConjoncture(@Param("nom") String nomSociete,
                                                                          @Param("email") String emailMembre,
                                                                          @Param("dateSoumission") Date dateSoumission);

    @Query("SELECT n FROM Note_ConjonctureVie_temporaire n JOIN n.membreAssurance m WHERE m.id_Membre = :idMembre")
    List<Note_ConjonctureVie_temporaire> findByMembreAssuranceId(Long idMembre);

    @Query("SELECT n FROM Note_ConjonctureVie_temporaire n JOIN n.membreAssurance m WHERE m.societe.id_societe = :idSociete")
    List<Note_ConjonctureVie_temporaire> findBySocieteId(Long idSociete);
}


