package com.DA.DA.repository;

import com.DA.DA.entite.NoteConjonctureIARDTemporaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface Note_ConjonctureIARD_temporaireRepository extends JpaRepository<NoteConjonctureIARDTemporaire, Long> {



    List<NoteConjonctureIARDTemporaire> findByDateSoumission(@Param("dateSoumission") Date dateSoumission);


    @Query("SELECT nc FROM NoteConjonctureIARDTemporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    NoteConjonctureIARDTemporaire findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                                         @Param("email") String emailMembre,
                                                                         @Param("dateSoumission") Date dateSoumission);


    @Query("SELECT nc FROM NoteConjonctureIARDTemporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<NoteConjonctureIARDTemporaire> findByCompagnie(@Param("nom") String nom);



    @Query("SELECT nc FROM NoteConjonctureIARDTemporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<NoteConjonctureIARDTemporaire> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);

    @Query("SELECT nc FROM NoteConjonctureIARDTemporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<NoteConjonctureIARDTemporaire> findNoteConjoncture(@Param("nom") String nomSociete,
                                                                         @Param("email") String emailMembre,
                                                                         @Param("dateSoumission") Date dateSoumission);

    @Query("SELECT n FROM NoteConjonctureIARDTemporaire n JOIN n.membreAssurance m WHERE m.id_Membre = :idMembre")
    List<NoteConjonctureIARDTemporaire> findByMembreAssuranceId(Long idMembre);

    @Query("SELECT n FROM NoteConjonctureIARDTemporaire n JOIN n.membreAssurance m WHERE m.societe.id_societe = :idSociete")
    List<NoteConjonctureIARDTemporaire> findBySocieteId(Long idSociete);

}
