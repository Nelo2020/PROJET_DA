package com.DA.DA.repository;


import com.DA.DA.entite.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface Note_ConjonctureVIERepository extends JpaRepository<Note_ConjonctureVIE, Long> {



    List<Note_ConjonctureVIE> findByDateSoumission(Date dateSoumission);

    @Query("SELECT nc FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<Note_ConjonctureVIE> findByCompagnie(@Param("nom") String nom);


    @Query("SELECT nc FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<Note_ConjonctureVIE> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);


    @Modifying
    @Query("DELETE FROM Note_ConjonctureVIE nc " +
            "WHERE nc.membreAssurance IN " +
            "(SELECT ma FROM MembreAssurance ma JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email) " +
            "AND nc.dateSoumission = :dateSoumission")
    void deleteByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                  @Param("email") String emailMembre,
                                                  @Param("dateSoumission") Date dateSoumission);


    @Query("SELECT nc FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ca WHERE ca.nom = :nom AND nc.dateSoumission = :dateSoumission")
    Note_ConjonctureVIE findByCompagnieAndDateSoumission(@Param("nom") String nom, @Param("dateSoumission") Date dateSoumission);





    @Query("SELECT nc FROM Note_ConjonctureVIE nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<Note_ConjonctureVIE> findNoteConjoncture(@Param("nom") String nomSociete,
                                                @Param("email") String emailMembre,
                                                @Param("dateSoumission") Date dateSoumission);




    @Query("SELECT COUNT(DISTINCT ma.societe) FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ma")
    int countDistinctSocietes();


        @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc WHERE nc.annee = :annee AND nc.trimestre = :trimestre")
        Long countDistinctSocietesByAnneeAndTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc WHERE nc.annee = :annee")
    int countDistinctSocietesByAnnee(@Param("annee") String annee);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc WHERE  nc.trimestre = :trimestre")
    int countDistinctSocietesByTrimestre(@Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTrimestreAndTypeAssurance(@Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE typeAssurance);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTypeAssurance(Type_AssuranceE typeAssurance);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND nc.annee = :annee AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTrimestreAndTypeAssurance(@Param("annee") String annee, @Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE libelles);


    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureVIE nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.annee = :annee AND  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTypeAssurance(String annee, Type_AssuranceE typeAssurance);


    @Query(value = "SELECT SUM(ncv.prime_emise_nette_annulations) " +
            "FROM NOTE_CONJONCTURE_VIE ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)",
            nativeQuery = true)
    Long sumPrimeEmiseNetteAnnulationForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);


    @Query(value = "SELECT SUM(ncv.commissions_servies_courtiers) " +
    "FROM NOTE_CONJONCTURE_VIE ncv " +
    "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
    "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumCommissionCourtierForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);
    @Query(value = "SELECT SUM(ncv.prestations_vie_effectivement_payees) " +
    "FROM NOTE_CONJONCTURE_VIE ncv " +
    "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
    "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumPrestationVieEffectivementPayesForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(ncv.provisions_mathematiques) " +
            "FROM NOTE_CONJONCTURE_VIE ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumProvisionMathForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(ncv.nombre_contrat_assurance) " +
            "FROM NOTE_CONJONCTURE_VIE ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumNombreContrat(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(ncv.effectif_du_personnel) " +
            "FROM NOTE_CONJONCTURE_VIE ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumEffectifPersonnelVie(@Param("annee") String annee, @Param("trimestre") String trimestre);


    @Query(value = "SELECT SUM(ncv.prestations_vie_effectivement_payees) " +
            "FROM NOTE_CONJONCTURE_VIE ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Double sumPrestationsVieEffectivementPayees(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT nc FROM Note_ConjonctureVIE nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    Note_ConjonctureVIE findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                                          @Param("email") String emailMembre,
                                                                          @Param("dateSoumission") Date dateSoumission);


//    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations, ncv.prestations_vie_effectivement_payees, ncv.nombre_contrat_assurance " +
//            "FROM Note_ConjonctureVIE ncv " +
//            "JOIN ncv.membreAssurance ma " +
//            "JOIN ma.societe s")
//    List<Object[]> findInformationsNoteConjonctureParSocieteVIE();

    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations, ncv.prestations_vie_effectivement_payees, ncv.nombre_contrat_assurance " +
            "FROM Note_ConjonctureVIE ncv " +
            "JOIN ncv.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)")
    List<Object[]> findInformationsNoteConjonctureParSocieteVIE(@Param("annee") String annee, @Param("trimestre") String trimestre);




    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations, ncv.prestations_vie_effectivement_payees, ncv.nombre_contrat_assurance " +
            "FROM Note_ConjonctureVIE ncv " +
            "JOIN ncv.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nomSociete " + // Ajout d'un espace ici
            "AND (:annee IS NULL OR ncv.annee = :annee) " + // Ajout d'un espace ici
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)")
    List<Object[]> findInformationsNoteConjonctureParSocieteVIENom(@Param("nomSociete") String nomSociete, @Param("annee") String annee, @Param("trimestre") String trimestre);


    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations " +
            "FROM Note_ConjonctureVIE ncv " +
            "JOIN ncv.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)")
    List<Object[]> TauxevolutionParSocieteVIE(@Param("annee") String annee, @Param("trimestre")String trimestre);



    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations " +
            "FROM Note_ConjonctureVIE ncv " +
            "JOIN ncv.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE ncv.annee = :annee " +
            "AND ncv.trimestre = :trimestre ")
    Long getPrimeEmiseTrimestre(@Param("annee") String annee, @Param("trimestre")String trimestre);


}






