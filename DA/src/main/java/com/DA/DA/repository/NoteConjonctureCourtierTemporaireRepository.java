package com.DA.DA.repository;

import com.DA.DA.entite.NoteConjonctureCourtierTemporaire;
import com.DA.DA.entite.Note_ConjonctureVie_temporaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface NoteConjonctureCourtierTemporaireRepository extends JpaRepository<NoteConjonctureCourtierTemporaire, Long> {

    @Query("SELECT nc FROM NoteConjonctureCourtierTemporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
   NoteConjonctureCourtierTemporaire findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                                         @Param("email") String emailMembre,
                                                                         @Param("dateSoumission") Date dateSoumission);
    @Query("SELECT nc FROM NoteConjonctureCourtierTemporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<NoteConjonctureCourtierTemporaire> findByCompagnie(@Param("nom") String nom);

    @Query("SELECT nc FROM   NoteConjonctureCourtierTemporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom AND nc.dateSoumission = :dateSoumission")
    NoteConjonctureCourtierTemporaire findByCompagnieAndDateSoumission(@Param("nom") String nom, @Param("dateSoumission") Date dateSoumission);
    List<NoteConjonctureCourtierTemporaire> findByDateSoumission(Date dateSoumission);



    @Query("SELECT nc FROM NoteConjonctureCourtierTemporaire nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<NoteConjonctureCourtierTemporaire> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);

    @Query("SELECT nc FROM NoteConjonctureCourtierTemporaire nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<NoteConjonctureCourtierTemporaire> findNoteConjoncture(@Param("nom") String nomSociete,
                                                                             @Param("email") String emailMembre,
                                                                             @Param("dateSoumission") Date dateSoumission);

 @Query("SELECT n FROM NoteConjonctureCourtierTemporaire n JOIN n.membreAssurance m WHERE m.id_Membre = :idMembre")
 List<NoteConjonctureCourtierTemporaire> findByMembreAssuranceId(Long idMembre);

 @Query("SELECT n FROM NoteConjonctureCourtierTemporaire n JOIN n.membreAssurance m WHERE m.societe.id_societe = :idSociete")
 List<NoteConjonctureCourtierTemporaire> findBySocieteId(Long idSociete);}
