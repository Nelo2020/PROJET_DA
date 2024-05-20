package com.DA.DA.repository;
import com.DA.DA.entite.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public interface Note_ConjonctureIARDRepository extends JpaRepository<Note_ConjonctureIARD, Long> {

    List<Note_ConjonctureIARD> findByDateSoumission(Date dateSoumission);

    @Query("SELECT nc FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nom ")
    List<Note_ConjonctureIARD> findByCompagnie(@Param("nom") String nom);
    @Modifying
    @Query("DELETE FROM Note_ConjonctureIARD nc " +
            "WHERE nc.membreAssurance IN " +
            "(SELECT ma FROM MembreAssurance ma JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email) " +
            "AND nc.dateSoumission = :dateSoumission")
    void deleteByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                  @Param("email") String emailMembre,
                                                  @Param("dateSoumission") Date dateSoumission);






    @Query("SELECT nc FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ca WHERE ca.societe.nom = :nomSociete AND ca.email = :emailMembre")
    List<Note_ConjonctureIARD> findByMembreAssurance(@Param("nomSociete") String nomSociete, @Param("emailMembre") String emailMembre);


    @Query("SELECT nc FROM Note_ConjonctureIARD nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    List<Note_ConjonctureIARD> findNoteConjoncture(@Param("nom") String nomSociete,
                                                       @Param("email") String emailMembre,
                                                       @Param("dateSoumission") Date dateSoumission);



    @Query("SELECT COUNT(DISTINCT ma.societe) FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma")
    int countDistinctSocietes();

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc WHERE nc.annee = :annee AND nc.trimestre = :trimestre")
    Long countDistinctSocietesByAnneeAndTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc WHERE nc.annee = :annee")
    int countDistinctSocietesByAnnee(@Param("annee") String annee);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc WHERE  nc.trimestre = :trimestre")
    int countDistinctSocietesByTrimestre(@Param("trimestre") String trimestre);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTrimestreAndTypeAssurance(@Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE typeAssurance);

    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByTypeAssurance(Type_AssuranceE typeAssurance);


    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.trimestre = :trimestre AND nc.annee = :annee AND s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTrimestreAndTypeAssurance(@Param("annee") String annee, @Param("trimestre") String trimestre, @Param("typeAssurance") Type_AssuranceE libelles);


    @Query("SELECT COUNT(DISTINCT nc.membreAssurance.societe) FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma JOIN ma.societe s WHERE nc.annee = :annee AND  s.typeAssurance.libelles = :typeAssurance")
    int countDistinctSocietesByAnneeAndTypeAssurance(String annee, Type_AssuranceE typeAssurance);

    @Query(value = "SELECT SUM(nci.prime_emise_nette_annulations) " +
            "FROM NOTE_CONJONCTURE_IARD nci " +
            "WHERE (:annee IS NULL OR nci.annee = :annee) " +
            "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
            nativeQuery = true)
    Long sumPrimeEmiseNetteAnnulationForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(nci.stock_de_sinistres_bons_a_payer) " +
    "FROM NOTE_CONJONCTURE_IARD nci " +
    "WHERE (:annee IS NULL OR nci.annee = :annee) " +
    "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumStock_de_sinistres_Bons_A_PayerForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(nci.commissions_servies_aux_courtiers) " +
            "FROM NOTE_CONJONCTURE_IARD nci " +
            "WHERE (:annee IS NULL OR nci.annee = :annee) " +
            "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
            nativeQuery = true)
    Long sumCommissionCourtierForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(nci.sinistre_a_payees) " +
    "FROM NOTE_CONJONCTURE_IARD nci " +
    "WHERE (:annee IS NULL OR nci.annee = :annee) " +
    "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumSinistre_A_PayeesForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(nci.dont_sisnistre_payee) " +
    "FROM NOTE_CONJONCTURE_IARD nci " +
    "WHERE (:annee IS NULL OR nci.annee = :annee) " +
    "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumDont_Sisnistre_PayeeForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT SUM(nci.credit_prime_emise+nci.caution_prime_emise) " +
    "FROM NOTE_CONJONCTURE_IARD nci " +
    "WHERE (:annee IS NULL OR nci.annee = :annee) " +
    "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)",
    nativeQuery = true)
    Long sumCredit_CautionForTrimestre(@Param("annee") String annee, @Param("trimestre") String trimestre);
//     @Query("SELECT s.nom,nci.prime_emise_nette_annulations,nci.sinistre_a_payees,nci.stock_de_sinistres_bons_a_payer,nci.dont_sisnistre_payee,nci.nombre_contrat_assurance" +
//     "FROM NOTE_CONJONCTURE_IARD nci " +
//     "JOIN ncv.membreAssurance ma " +
//     "JOIN ma.societe s " +
//     "WHERE (:annee IS NULL OR nci.annee = :annee) " +
//     "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)")
//     List<Object[]> findInformationsNoteConjonctureParSocieteIARD(@Param("annee") String annee, @Param("trimestre") String trimestre);
    @Query(value = "SELECT SUM(ncv.effectif_du_personnel) " +
            "FROM NOTE_CONJONCTURE_IARD ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumEffectifPersonnelIard(@Param("annee") String annee, @Param("trimestre") String trimestre);


    @Query(value = "SELECT SUM(nci.dont_sisnistre_payee) " +
            "FROM Note_ConjonctureIARD nci " +
            "WHERE (:annee IS NULL OR nci.annee = :annee) " +
            "AND (:trimestre IS NULL OR nci.trimestre = :trimestre)")
    Double sumDontSinistrePayee(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query("SELECT nc FROM Note_ConjonctureIARD nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE s.nom = :nom AND ma.email = :email AND nc.dateSoumission = :dateSoumission")
    Note_ConjonctureIARD findByMembreAssuranceAndDateSoumission(@Param("nom") String nomSociete,
                                                               @Param("email") String emailMembre,
                                                               @Param("dateSoumission") Date dateSoumission);

    @Query(value = "SELECT SUM(ncv.nombre_de_contrats_assurance) " +
            "FROM NOTE_CONJONCTURE_IARD ncv " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)", nativeQuery = true)
    Long sumNombreContrat(@Param("annee") String annee, @Param("trimestre") String trimestre);

    @Query(value = "SELECT nc.prime_emise_nette_annulations, nc.nombre_de_contrats_assurance, s.nom " +
            "FROM Note_ConjonctureIARD nc " +
            "JOIN nc.membreAssurance ma " +
            "JOIN ma.societe s", nativeQuery = true)
    List<Object[]> findPrimeEtNombreContratsParSociete();


    @Query("SELECT DISTINCT s.nom FROM Note_ConjonctureIARD nc JOIN nc.membreAssurance ma JOIN ma.societe s")
    List<String> findDistinctNomSocietes();

    @Query("SELECT s.nom, nc.prime_emise_nette_annulations, nc.sinistre_a_payees, nc.stock_de_sinistres_bons_a_payer, nc.dont_sisnistre_payee, nc.nombre_de_contrats_assurance " +
           "FROM Note_ConjonctureIARD nc " +
           "JOIN nc.membreAssurance ma " +
           "JOIN ma.societe s " +
           "WHERE (:annee IS NULL OR nc.annee = :annee) " +
           "AND (:trimestre IS NULL OR nc.trimestre = :trimestre)")
    List<Object[]> findInformationsNoteConjonctureParSocieteIARD(@Param("annee") String annee, @Param("trimestre") String trimestre);



    @Query("SELECT s.nom, ncv.prime_emise_nette_annulations " +
            "FROM Note_ConjonctureIARD ncv " +
            "JOIN ncv.membreAssurance ma " +
            "JOIN ma.societe s " +
            "WHERE (:annee IS NULL OR ncv.annee = :annee) " +
            "AND (:trimestre IS NULL OR ncv.trimestre = :trimestre)")
    List<Object[]> TauxevolutionParSocieteIARD(@Param("annee") String annee, @Param("trimestre")String trimestre);

}
