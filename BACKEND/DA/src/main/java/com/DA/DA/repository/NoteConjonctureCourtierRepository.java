package com.DA.DA.repository;

import com.DA.DA.entite.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface NoteConjonctureCourtierRepository extends JpaRepository<NoteConjonctureCourtier, Long> {
    @Query("SELECT nc FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<NoteConjonctureCourtier> findByCompagnie(@Param("nom") String nom);


    @Query("SELECT nc FROM   NoteConjonctureCourtier nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom AND nc.dateSoumission = :dateSoumission")
    NoteConjonctureCourtier findByCompagnieAndDateSoumission(@Param("nom") String nom, @Param("dateSoumission") Date dateSoumission);

    List<NoteConjonctureCourtier> findByDateSoumission(Date dateSoumission);

    @Modifying
    @Query("DELETE FROM NoteConjonctureCourtier nc " +
            "WHERE nc.membreAssurance IN " +
            "(SELECT ma FROM MembreAssurance ma JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email) " +
            "AND nc.dateSoumission = :dateSoumission")
    void deleteByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                  @Param("email") String emailMembre,
                                                  @Param("dateSoumission") Date dateSoumission);

    @Query("SELECT nc FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<NoteConjonctureCourtier> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);




    @Query("SELECT nc FROM NoteConjonctureCourtier nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<NoteConjonctureCourtier> findNoteConjoncture(@Param("nom") String nomSociete,
                                                          @Param("email") String emailMembre,
                                                          @Param("dateSoumission") Date dateSoumission);



    @Query("SELECT COUNT(DISTINCT ma.societe) FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ma")
    int countDistinctSocietes();
    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc WHERE nc.annee = :annee AND nc.trimestre = :trimestre")
    int countDistinctSocietesByAnneeAndTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc WHERE nc.annee = :annee")
    int countDistinctSocietesByAnnee(@Param("annee") String annee);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc WHERE  nc.trimestre = :trimestre")
    int countDistinctSocietesByTrimestre(@Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTrimestreAndTypeAssurance(@Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE typeAssurance);


    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTypeAssurance(Type_AssuranceE typeAssurance);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND nc.annee = :annee AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTrimestreAndTypeAssurance(@Param("annee") String annee, @Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE libelles);


    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM NoteConjonctureCourtier nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.annee = :annee AND  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTypeAssurance(String annee, Type_AssuranceE typeAssurance);

    @Query(value = "SELECT SUM(ncv.total_personnel) " +
            "FROM NOTE_CONJONCTURE_COURTIER ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumEffectifPersonnelCourtier(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT nc FROM NoteConjonctureCourtier nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    NoteConjonctureCourtier findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,

                                                                @Param("email") String emailMembre,
                                                                @Param("dateSoumission") Date dateSoumission);

    @Query(value = "SELECT SUM(ncv.emissions_primes) " +
            "FROM NOTE_CONJONCTURE_COURTIER ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)",
            nativeQuery = true)
    Double sumPrimeEmiseNetteAnnulationForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

//    @Query("SELECT s.nom, nc.emissions_primes " +
//            "FROM NoteConjonctureCourtier nc " +
//            "JOIN nc.membreAssurance ma " +
//            "JOIN ma.societe s")
//    List<Object[]> findInformationsNoteConjonctureParSocieteCourtier();

    @Query("SELECT s.nom, nc.emissions_primes " +
            "FROM NoteConjonctureCourtier nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE (:annee IS NULL OR nc.annee = :annee) " +
            "AND (:trimestre IS NULL OR nc.trimestre = :trimestre)")
    List<Object[]> findInformationsNoteConjonctureParSocieteCourtier(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT s.nom, nc.emissions_primes " +
            "FROM NoteConjonctureCourtier nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nomSociete " + // Ajout d'un espace ici
            "AND (:annee IS NULL OR nc.annee = :annee) " + // Ajout d'un espace ici
            "AND (:trimestre IS NULL OR nc.trimestre = :trimestre)")
    List<Object[]> findInformationsNoteConjonctureParSocieteCourtierNom(@Param("nomSociete") String nomSociete, @Param("annee") String annee, @Param("trimestre") String trimestre);
}
