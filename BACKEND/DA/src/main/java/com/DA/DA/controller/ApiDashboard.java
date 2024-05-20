package com.DA.DA.controller;

import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import com.DA.DA.entite.Type_AssuranceE;
import com.DA.DA.repository.*;
import com.DA.DA.security.JwtServiceMembre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashbord")
public class ApiDashboard {

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private Note_ConjonctureVIERepository noteConjonctureVIERepository;
    @Autowired
    private Note_ConjonctureIARDRepository noteConjonctureIARDRepository;
    @Autowired
    private NoteConjonctureCourtierRepository noteConjonctureCourtierRepository;
    @Autowired
    private MembreAssuranceRepository membreAssuranceRepository;
    @Autowired
    private JwtServiceMembre jwtServiceMembre;
    @Autowired
    private SocieteRepository societeRepository;
    @Autowired
    private DaRepository daRepository;



    // dashboard vu_ ensemble
    @GetMapping("/vu_ensemble/{annee}/{trimestre}")
    private Map<String, Long> getVuensemble(
            @PathVariable(value = "annee", required = false) String annee,
            @PathVariable(value = "trimestre", required = false) String trimestre) {
        Map<String, Long> vu_ensemble = new HashMap<>();

        if (annee != null && trimestre != null) {
            int anneePrecedente = Integer.parseInt(annee) - 1;
            //taux d evolution prime emise nette d'annulation
            Long primeAnneeselectionneeVIE=noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
            Long primeAnneeselectionnepreccedentVIE=noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long primeAnneeselectionneeIARD=noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
            Long primeAnneeselectionnepreccedentIARD=noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
            //taux d evolution sinistres payes
            Long prestationAnneeselectionneeVIE=noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(annee, trimestre);
            Long prestationAnneepreccedentselectionneeVIE=noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long sinistreAnneeselectionneIARD=noteConjonctureIARDRepository.sumSinistre_A_PayeesForTrimestre(annee, trimestre);
            Long sinistreAnneeselectionnepreccedentIARD=noteConjonctureIARDRepository.sumSinistre_A_PayeesForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long stocksinistreAnneeselectionneeIARD=noteConjonctureIARDRepository.sumStock_de_sinistres_Bons_A_PayerForTrimestre(annee, trimestre);
            Long stocksinistreAnneeselectionneeIARDs=noteConjonctureIARDRepository.sumStock_de_sinistres_Bons_A_PayerForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long provisionAnneepreccedentselectionneVIE=noteConjonctureVIERepository.sumProvisionMathForTrimestre(annee, trimestre);
            Long provisionAnneepreccedentselectionneVIEs=noteConjonctureVIERepository.sumProvisionMathForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long dontsinistreAnneeselectionneeIARD=noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(annee, trimestre);
            Long dontsinistreAnneepreccedentselectionneeIARD=noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(String.valueOf(anneePrecedente), trimestre);
            Long tauxEvolutionVIE=((primeAnneeselectionneeVIE-primeAnneeselectionnepreccedentVIE)/primeAnneeselectionnepreccedentVIE)*100;
            Long tauxEvolutionIARD=((primeAnneeselectionneeIARD-primeAnneeselectionnepreccedentIARD)/primeAnneeselectionnepreccedentIARD)*100;
            Long tauxEvolutionCA=tauxEvolutionIARD+tauxEvolutionVIE;
            Long prestationVIE=((prestationAnneeselectionneeVIE-prestationAnneepreccedentselectionneeVIE)/prestationAnneepreccedentselectionneeVIE)*100;
            Long sinistreApayes=((sinistreAnneeselectionneIARD-sinistreAnneeselectionnepreccedentIARD)/sinistreAnneeselectionnepreccedentIARD)*100;
            Long stock=((stocksinistreAnneeselectionneeIARD-stocksinistreAnneeselectionneeIARDs)/stocksinistreAnneeselectionneeIARDs)*100;
            Long provision=(provisionAnneepreccedentselectionneVIE-provisionAnneepreccedentselectionneVIEs)/provisionAnneepreccedentselectionneVIEs*100;
            Long dontsinistrespayes=((dontsinistreAnneeselectionneeIARD-dontsinistreAnneepreccedentselectionneeIARD)/dontsinistreAnneepreccedentselectionneeIARD)*100;


            Long totalSocietesIARD = noteConjonctureIARDRepository.countDistinctSocietesByAnneeAndTrimestre(annee, trimestre);
            Long totalSocietesVIE = noteConjonctureVIERepository.countDistinctSocietesByAnneeAndTrimestre(annee, trimestre);
            Long totalSocietes = totalSocietesIARD + totalSocietesVIE;

            Long caVie = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
            Long caIard = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
            Long catotal = caVie + caIard;

            Long comIard = noteConjonctureIARDRepository.sumCommissionCourtierForTrimestre(annee, trimestre);
            Long comVie = noteConjonctureVIERepository.sumCommissionCourtierForTrimestre(annee, trimestre);
            Long comtotal = comIard + comVie;

            Long sinistrepayesVie = noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(annee, trimestre);
            Long sinistrepayesIard = noteConjonctureIARDRepository.sumSinistre_A_PayeesForTrimestre(annee, trimestre);
            Long sinistreTotal = sinistrepayesVie + sinistrepayesIard;

            Long contratVie = noteConjonctureVIERepository.sumNombreContrat(annee, trimestre);
            Long contratIard = noteConjonctureIARDRepository.sumNombreContrat(annee, trimestre);
            Long contrattotal = contratVie + contratIard;

            Long effectifIard = noteConjonctureIARDRepository.sumEffectifPersonnelIard(annee, trimestre);
            Long effectifVie = noteConjonctureVIERepository.sumEffectifPersonnelVie(annee, trimestre);
            Long effectiftotal = effectifIard + effectifVie;

            Long sinistre_bon_a_paye = noteConjonctureIARDRepository.sumStock_de_sinistres_Bons_A_PayerForTrimestre(annee, trimestre);
            Long provision_mathematique = noteConjonctureVIERepository.sumProvisionMathForTrimestre(annee, trimestre);
            Long sinistre_payes = noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(annee, trimestre);

            Long credit_caution = noteConjonctureIARDRepository.sumCredit_CautionForTrimestre(annee, trimestre);

            vu_ensemble.put("Total_Societes", totalSocietes);
            vu_ensemble.put("Nombre_Total_Societes_IARD", totalSocietesIARD);
            vu_ensemble.put("Nombre_Total_Societes_VIE", totalSocietesVIE);
            vu_ensemble.put("CA_total", catotal);
            vu_ensemble.put("CA_VIE", caVie);
            vu_ensemble.put("CA_IARD", caIard);
            vu_ensemble.put("Commission_courtiers_total", comtotal);
            vu_ensemble.put("Commission_courtiers_VIE", comVie);
            vu_ensemble.put("Commission_courtiers_IARD", comIard);
            vu_ensemble.put("sinistres_payes_total", sinistreTotal);
            vu_ensemble.put("sinistres_payes_VIE", sinistrepayesVie);
            vu_ensemble.put("sinistres_payes_IARD", sinistrepayesIard);
            vu_ensemble.put("nombre_contrat_total", contrattotal);
            vu_ensemble.put("nombre_contrat_VIE", contratVie);
            vu_ensemble.put("nombre_contrat_IARD", contratIard);
            vu_ensemble.put("effectif_personnel_total", effectiftotal);
            vu_ensemble.put("effectif_personnel_VIE", effectifVie);
            vu_ensemble.put("effectif_personnel_IARD", effectifIard);
            vu_ensemble.put("provision_mathematiques", provision_mathematique);
            vu_ensemble.put("sinistre_bon_a_payees", sinistre_bon_a_paye);
            vu_ensemble.put("sinistre_payees", sinistre_payes);
            vu_ensemble.put("credit-caution", credit_caution);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations_VIE", tauxEvolutionVIE);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations_IARD", tauxEvolutionIARD);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations", tauxEvolutionCA);
            vu_ensemble.put("Prestations_vie_effectivement", prestationVIE);
            vu_ensemble.put("Taux_Evolution_sinistres_a_payer", sinistreApayes);
            vu_ensemble.put("Taux_Evolution_stock_sinistres_a_payer", stock);
            vu_ensemble.put("Taux_Evolution_provision_mathematiques", provision);
            vu_ensemble.put("Taux_Evolution_dont_sinistres_a_payer", dontsinistrespayes);
        } else {
            vu_ensemble.put("Total_Societes", 0L);
            vu_ensemble.put("Nombre_Total_Societes_IARD", 0L);
            vu_ensemble.put("Nombre_Total_Societes_VIE", 0L);
            vu_ensemble.put("CA_total", 0L);
            vu_ensemble.put("CA_VIE", 0L);
            vu_ensemble.put("CA_IARD", 0L);
            vu_ensemble.put("Commission_courtiers_total", 0L);
            vu_ensemble.put("Commission_courtiers_VIE", 0L);
            vu_ensemble.put("Commission_courtiers_IARD", 0L);
            vu_ensemble.put("sinistres_payes_total", 0L);
            vu_ensemble.put("sinistres_payes_VIE", 0L);
            vu_ensemble.put("sinistres_payes_IARD", 0L);
            vu_ensemble.put("nombre_contrat_total", 0L);
            vu_ensemble.put("nombre_contrat_VIE", 0L);
            vu_ensemble.put("nombre_contrat_IARD", 0L);
            vu_ensemble.put("effectif_personnel_total", 0L);
            vu_ensemble.put("effectif_personnel_VIE", 0L);
            vu_ensemble.put("effectif_personnel_IARD", 0L);
            vu_ensemble.put("provision_mathematiques", 0L);
            vu_ensemble.put("sinistre_bon_a_payees", 0L);
            vu_ensemble.put("sinistre_payees", 0L);
            vu_ensemble.put("credit-caution", 0L);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations", 0L);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations_IARD", 0L);
            vu_ensemble.put("Taux_Evolution_primes_emises_nettes_annulations", 0L);
            vu_ensemble.put("Prestations_vie_effectivement", 0L);
            vu_ensemble.put("Taux_Evolution_sinistres_a_payer", 0L);
            vu_ensemble.put("Taux_Evolution_stock_sinistres_a_payer", 0L);
            vu_ensemble.put("Taux_Evolution_provision_mathematiques", 0L);
            vu_ensemble.put("Taux_Evolution_dont_sinistres_a_payer", 0L);
        }

        return vu_ensemble;
    }

    // dashboard Iard
    @GetMapping("/IARD/{annee}/{trimestre}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getInformationIARD(
            @PathVariable(value = "annee", required = false) String annee,
            @PathVariable(value = "trimestre", required = false) String trimestre) {
        List<Object[]> resultsIARD = noteConjonctureIARDRepository.findInformationsNoteConjonctureParSocieteIARD(annee, trimestre);
        List<Map<String, Object>> formattedResultsIARD = new ArrayList<>();
        Map<String, List<Map<String, Object>>> iard = new HashMap<>();

        if (annee != null && trimestre != null) {
            for (Object[] result : resultsIARD) {
                Map<String, Object> formattedResult = new HashMap<>();
                String nomSociete = (String) result[0];
                Double primeEmiseNette = (Double) result[1];
                Double sinistreAPayees = (Double) result[2];
                Double stockDeSinistresBonsAPayer = (Double) result[3];
                Double dontSinistrePayee = (Double) result[4];
                Long nombreContrats = (Long) result[5];
                formattedResult.put("nom_societe", nomSociete);
                formattedResult.put("prime_emise_nette", primeEmiseNette);
                formattedResult.put("sinistre_a_payees", sinistreAPayees);
                formattedResult.put("stock_de_sinistres_bons_a_payer", stockDeSinistresBonsAPayer);
                formattedResult.put("dont_sinistre_payee", dontSinistrePayee);
                formattedResult.put("nombre_contrat", nombreContrats);
                formattedResultsIARD.add(formattedResult);
            }
        } else {
            for (Object[] result : resultsIARD) {
                Map<String, Object> formattedResult = new HashMap<>();
                String nomSociete = (String) result[0];
                formattedResult.put("nom_societe", nomSociete);
                formattedResult.put("prime_emise_nette", 0L);
                formattedResult.put("sinistre_a_payees", 0L);
                formattedResult.put("stock_de_sinistres_bons_a_payer", 0L);
                formattedResult.put("dont_sinistre_payee", 0L);
                formattedResult.put("nombre_contrat", 0L);
                formattedResultsIARD.add(formattedResult);
            }
        }

        iard.put("IARD", formattedResultsIARD);
        return ResponseEntity.ok().body(iard);
    }

    @GetMapping("/VIE/{annee}/{trimestre}")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getInformationVIE(
            @PathVariable(value = "annee", required = false) String annee,
            @PathVariable(value = "trimestre", required = false) String trimestre) {
        Map<String, List<Map<String, Object>>> vie = new HashMap<>();
        List<Map<String, Object>> formattedResultsVIE = new ArrayList<>();
        List<Object[]> resultsVIE = noteConjonctureVIERepository.findInformationsNoteConjonctureParSocieteVIE(annee, trimestre);
        if (annee != null && trimestre != null) {
            for (Object[] result : resultsVIE) {
                Map<String, Object> formattedResult = new HashMap<>();
                String nomSociete = (String) result[0];
                Double primeEmiseNette = (Double) result[1];
                Double prestationsVieEffectivementPayees = (Double) result[2];
                Long nombreContrats = (Long) result[3];
                formattedResult.put("nom_societe", nomSociete);
                formattedResult.put("prime_emise_nette", primeEmiseNette);
                formattedResult.put("prestations_vie_effectivement_payees", prestationsVieEffectivementPayees);
                formattedResult.put("nombre_contrat", nombreContrats);
                formattedResultsVIE.add(formattedResult);
            }
        } else {
            for (Object[] result : resultsVIE) {
                Map<String, Object> formattedResult = new HashMap<>();
                String nomSociete = (String) result[0];
                formattedResult.put("nom_societe", nomSociete);
                formattedResult.put("prime_emise_nette", 0L);
                formattedResult.put("prestations_vie_effectivement_payees", 0L);
                formattedResult.put("nombre_contrat", 0L);
                formattedResultsVIE.add(formattedResult);
            }
        }

        vie.put("VIE", formattedResultsVIE);
        return ResponseEntity.ok().body(vie);
    }


// @GetMapping("/calculerTauxEvolutionVIE")
// public Map<String, Object> getTauxEvolutionVIE(@RequestParam(required = false) String annee,
//                                             @RequestParam(required = false) String trimestre) {
//     List<Object[]> results = noteConjonctureVIERepository.TauxevolutionParSocieteVIE(annee, trimestre);
//     Map<String, Object> response = new HashMap<>();
//     response.put("annee", annee);
//     response.put("trimestre", trimestre);

//     List<Map<String, Object>> data = new ArrayList<>();

//     for (Object[] result : results) {
//         Map<String, Object> entry = new HashMap<>();
//         String nomSociete = (String) result[0];
//         Double primeEmiseTrimestre = (Double) result[1];

//         entry.put("nom_societe", nomSociete);
//         entry.put("prime_emise_trimestre", primeEmiseTrimestre);

//         // Obtention du trimestre précédent
//         Long primeEmiseTrimestrePrecedent = null;
//         if(annee!=null && trimestre!=null){
//             int anneePrecedente = Integer.parseInt(annee) - 1;
//             Long primeAnneeselectionnee=noteConjonctureVIERepository.getPrimeEmiseTrimestre(annee, trimestre);
//             Long primeAnneeselectionnepreccedent=noteConjonctureVIERepository.getPrimeEmiseTrimestre(String.valueOf(anneePrecedente), trimestre);
//             Long tauxEvolution=((primeAnneeselectionnee-primeAnneeselectionnepreccedent)/primeAnneeselectionnepreccedent)*100;

//         }else{

//         }
//     }
//     response.put("resultats", data);

//     return response;
// }


    // //calcul du taux d'evolution de chaque societe IARD
    // @GetMapping("/calculerTauxEvolutionIARD")
    // public Map<String, Object> getTauxEvolutionIARD(@RequestParam(required = false) String annee,
    //                                             @RequestParam(required = false) String trimestre) {
    //     List<Object[]> results = noteConjonctureIARDRepository.TauxevolutionParSocieteIARD(annee, trimestre);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);

    //     List<Map<String, Object>> data = new ArrayList<>();

    //     for (Object[] result : results) {
    //         Map<String, Object> entry = new HashMap<>();
    //         String nomSociete = (String) result[0];
    //         Double primeEmiseTrimestre = (Double) result[1];

    //         entry.put("nom_societe", nomSociete);
    //         entry.put("prime_emise_trimestre", primeEmiseTrimestre);

    //         // Obtention du trimestre précédent
    //         Double primeEmiseTrimestrePrecedent = null;
    //         if (trimestre != null && trimestre.equals("Trimestre 1")) {
    //             // Calcul de l'année précédente
    //             int anneePrecedente = Integer.parseInt(annee) - 1;
    //             // Obtention de la prime émise du trimestre 4 de l'année précédente
    //             primeEmiseTrimestrePrecedent = noteConjonctureIARDRepository.getPrimeEmiseTrimestrePrecedent(String.valueOf(anneePrecedente), "Trimestre 4", nomSociete);
    //             if (primeEmiseTrimestrePrecedent == null) {
    //                 primeEmiseTrimestrePrecedent = 0.0; // Si la prime précédente est nulle, le taux est également de 0
    //             }
    //             // Mettre à jour l'année avec l'année précédente
    //             response.put("annee", String.valueOf(anneePrecedente));
    //         } else if (trimestre != null && !trimestre.equals("1")) {
    //             // Obtention du trimestre précédent
    //             String trimestrePrecedent = getTrimestrePrecedent(trimestre);
    //             // Obtention de la prime émise du trimestre précédent
    //             primeEmiseTrimestrePrecedent = noteConjonctureIARDRepository.getPrimeEmiseTrimestrePrecedent(annee, trimestrePrecedent, nomSociete);
    //             if (primeEmiseTrimestrePrecedent == null) {
    //                 primeEmiseTrimestrePrecedent = 0.0; // Si la prime précédente est nulle, le taux est également de 0
    //             }
    //         }

    //         // Calcul du taux d'évolution
    //         double tauxEvolution = 0.0;
    //         if (primeEmiseTrimestrePrecedent != null && primeEmiseTrimestrePrecedent != 0.0) {
    //             tauxEvolution = ((primeEmiseTrimestre - primeEmiseTrimestrePrecedent) / primeEmiseTrimestrePrecedent) * 100;
    //         }
    //         entry.put("prime_emise_trimestre_precedent", primeEmiseTrimestrePrecedent);
    //         entry.put("taux_evolution", tauxEvolution);

    //         data.add(entry);
    //     }

    //     response.put("resultats", data);

    //     return response;
    // }

    // //calcul du taux d'evolution de chaque societe IARD annee successive
    // @GetMapping("/calculerTauxEvolutionIARDAnnee")
    // public Map<String, Object> getTauxEvolutionIARDAnnee(@RequestParam(required = false) String annee,
    //                                                      @RequestParam(required = false) String trimestre) {
    //     List<Object[]> results = noteConjonctureIARDRepository.TauxevolutionParSocieteIARD(annee, trimestre);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);

    //     List<Map<String, Object>> data = new ArrayList<>();

    //     for (Object[] result : results) {
    //         Map<String, Object> entry = new HashMap<>();
    //         String nomSociete = (String) result[0];
    //         Double primeEmiseTrimestre = (Double) result[1];

    //         entry.put("nom_societe", nomSociete);
    //         entry.put("prime_emise_trimestre", primeEmiseTrimestre);

    //         // Obtention de l'année précédente
    //         int anneePrecedente = Integer.parseInt(annee) - 1;

    //         // Obtention de la prime émise du même trimestre de l'année précédente
    //         Double primeEmiseTrimestrePrecedent = noteConjonctureIARDRepository.getPrimeEmiseTrimestrePrecedent(String.valueOf(anneePrecedente), trimestre, nomSociete);
    //         if (primeEmiseTrimestrePrecedent == null) {
    //             primeEmiseTrimestrePrecedent = 0.0; // Si la prime précédente est nulle, le taux est également de 0
    //         }

    //         // Calcul du taux d'évolution
    //         double tauxEvolution = 0.0;
    //         if (primeEmiseTrimestrePrecedent != null && primeEmiseTrimestrePrecedent != 0.0) {
    //             tauxEvolution = ((primeEmiseTrimestre - primeEmiseTrimestrePrecedent) / primeEmiseTrimestrePrecedent) * 100;
    //         }
    //         entry.put("prime_emise_trimestre_precedent", primeEmiseTrimestrePrecedent);
    //         entry.put("taux_evolution", tauxEvolution);

    //         data.add(entry);
    //     }

    //     response.put("resultats", data);

    //     return response;
    // }


    // //calcul du taux d'evolution de toutes les soicietes vies mm annee trimestre successif
    // @GetMapping("/sumPrimeEmiseNetteAnnulationVIE")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationVIE(@RequestParam(required = false) String annee,
    //                                                            @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long sommeActuelle = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("somme_actuelle", sommeActuelle);

    //     // Obtenir le trimestre précédent de la même année
    //     String trimestrePrecedent = getTrimestrePrecedent(trimestre);
    //     Long sommeTrimestrePrecedent = null;
    //     if (trimestrePrecedent != null) {
    //         sommeTrimestrePrecedent = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestrePrecedent);
    //     }
    //     response.put("somme_trimestre_precedent", sommeTrimestrePrecedent);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeTrimestrePrecedent != null && sommeTrimestrePrecedent != 0) {
    //         tauxEvolution = ((double) (sommeActuelle - sommeTrimestrePrecedent) / sommeTrimestrePrecedent) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }

    // //calcul du taux d'evolution de toutes les soicietes VIE mm trimestre annee successive
    // @GetMapping("/sumPrimeEmiseNetteAnnulationVIEAnnee")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationVIEAnnee(@RequestParam(required = false) String annee,
    //                                                                     @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long sommeActuelle = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("somme_actuelle", sommeActuelle);

    //     // Obtenir le trimestre précédent de l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;
    //     Long somme_annee_precedente = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     response.put("somme_annee_precedente", somme_annee_precedente);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeActuelle != null && somme_annee_precedente != null && somme_annee_precedente != 0) {
    //         tauxEvolution = ((double) (sommeActuelle - somme_annee_precedente) / somme_annee_precedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }


    // //calcul du taux d'evolution de toutes les soicietes IARD mm annee trimestre successif
    // @GetMapping("/sumPrimeEmiseNetteAnnulationIARD")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationIARD(@RequestParam(required = false) String annee,
    //                                                                @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long sommeActuelle = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("somme_actuelle", sommeActuelle);

    //     // Obtenir le trimestre précédent de la même année
    //     String trimestrePrecedent = getTrimestrePrecedent(trimestre);
    //     Long sommeTrimestrePrecedent = null;
    //     if (trimestrePrecedent != null) {
    //         sommeTrimestrePrecedent = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestrePrecedent);
    //     }
    //     response.put("somme_trimestre_precedent", sommeTrimestrePrecedent);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeActuelle != null && sommeTrimestrePrecedent != null && sommeTrimestrePrecedent != 0) {
    //         tauxEvolution = ((double) (sommeActuelle - sommeTrimestrePrecedent) / sommeTrimestrePrecedent) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }


    // //calcul du taux d'evolution de toutes les soicietes IARD mm trimestre annee successive
    // @GetMapping("/sumPrimeEmiseNetteAnnulationIARDAnnee")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationIARDAnnee(@RequestParam(required = false) String annee,
    //                                                                @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long sommeActuelle = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("somme_actuelle", sommeActuelle);

    //     // Obtenir le trimestre précédent de l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;
    //     Long somme_annee_precedente = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     response.put("somme_annee_precedente", somme_annee_precedente);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeActuelle != null && somme_annee_precedente != null && somme_annee_precedente != 0) {
    //         tauxEvolution = ((double) (sommeActuelle - somme_annee_precedente) / somme_annee_precedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }


    // @GetMapping("/sumPrimeEmiseNetteAnnulationTotal")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationTotal(@RequestParam(required = false) String annee,
    //                                                                 @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme totale des primes Vie et IARD pour le trimestre actuel
    //     Long sommeVie = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     Long sommeIARD = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     Long sommeTotale = (sommeVie != null ? sommeVie : 0) + (sommeIARD != null ? sommeIARD : 0);
    //     response.put("somme_totale", sommeTotale);

    //     // Obtenir la somme totale des primes Vie et IARD pour le trimestre précédent
    //     String trimestrePrecedent = getTrimestrePrecedent(trimestre);
    //     Long sommeVieTrimestrePrecedent = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestrePrecedent);
    //     Long sommeIARDTrimestrePrecedent = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestrePrecedent);
    //     Long sommeTotaleTrimestrePrecedent = (sommeVieTrimestrePrecedent != null ? sommeVieTrimestrePrecedent : 0) + (sommeIARDTrimestrePrecedent != null ? sommeIARDTrimestrePrecedent : 0);
    //     response.put("somme_totale_trimestre_precedent", sommeTotaleTrimestrePrecedent);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeTotaleTrimestrePrecedent != null && sommeTotaleTrimestrePrecedent != 0) {
    //         tauxEvolution = ((double) (sommeTotale - sommeTotaleTrimestrePrecedent) / sommeTotaleTrimestrePrecedent) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }
    // @GetMapping("/sumPrimeEmiseNetteAnnulationTotalAnnee")
    // public Map<String, Object> getSumPrimeEmiseNetteAnnulationTotalAnnee(@RequestParam(required = false) String annee,
    //                                                                 @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme totale des primes Vie et IARD pour le trimestre actuel de l'année en cours
    //     Long sommeVie = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     Long sommeIARD = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(annee, trimestre);
    //     Long sommeTotale = (sommeVie != null ? sommeVie : 0) + (sommeIARD != null ? sommeIARD : 0);
    //     response.put("somme_totale", sommeTotale);

    //     // Obtenir l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;

    //     // Obtenir la somme totale des primes Vie et IARD pour le même trimestre de l'année précédente
    //     Long sommeVieAnneePrecedente = noteConjonctureVIERepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     Long sommeIARDAnneePrecedente = noteConjonctureIARDRepository.sumPrimeEmiseNetteAnnulationForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     Long sommeTotaleAnneePrecedente = (sommeVieAnneePrecedente != null ? sommeVieAnneePrecedente : 0) + (sommeIARDAnneePrecedente != null ? sommeIARDAnneePrecedente : 0);
    //     response.put("somme_totale_annee_precedente", sommeTotaleAnneePrecedente);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeTotaleAnneePrecedente != null && sommeTotaleAnneePrecedente != 0) {
    //         tauxEvolution = ((double) (sommeTotale - sommeTotaleAnneePrecedente) / sommeTotaleAnneePrecedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }

    // //calcul du taux d'evolution de toutes les soicietes IARD sinistre  mm trimestre annee successive
    // @GetMapping("/sumSinistreIARDAnnee")
    // public Map<String, Object> getSumsinistreIARDAnnee(@RequestParam(required = false) String annee,
    //                                                    @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long sinistreActuelle = noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("sinistreActuelle", sinistreActuelle);

    //     // Obtenir le trimestre précédent de l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;
    //     Long sinistre_annee_precedente = noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     response.put("sinistre_annee_precedente", sinistre_annee_precedente != null ? sinistre_annee_precedente : 0);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sinistreActuelle != null && sinistre_annee_precedente != null && sinistre_annee_precedente != 0) {
    //         tauxEvolution = ((double) (sinistreActuelle - sinistre_annee_precedente) / sinistre_annee_precedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }

    // @GetMapping("/sumprestationVIEAnnee")
    // public Map<String, Object> getSumPrestationVIEAnnee(@RequestParam(required = false) String annee,
    //                                                     @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // Obtenir la somme pour le trimestre actuel
    //     Long prestation_actuelle = noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(annee, trimestre);
    //     response.put("annee", annee);
    //     response.put("trimestre", trimestre);
    //     response.put("prestation_actuelle", prestation_actuelle != null ? prestation_actuelle : 0);

    //     // Obtenir le trimestre précédent de l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;
    //     Long prestation_annee_precedente = noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     response.put("prestation_annee_precedente", prestation_annee_precedente != null ? prestation_annee_precedente : 0);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (prestation_actuelle != null && prestation_annee_precedente != null && prestation_annee_precedente != 0) {
    //         tauxEvolution = ((double) (prestation_actuelle - prestation_annee_precedente) / prestation_annee_precedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);

    //     return response;
    // }


    // @GetMapping("/sumsinistreEtprestation")
    // public Map<String, Object> getsumsinistreEtprestation(@RequestParam(required = false) String annee,
    //                                                                      @RequestParam(required = false) String trimestre) {
    //     Map<String, Object> response = new HashMap<>();

    //     // // Obtenir la somme totale des primes Vie et IARD pour le trimestre actuel de l'année en cours
    //     // Long sommeVie = noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(annee, trimestre);
    //     // Long sommeIARD = noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(annee, trimestre);
    //     // Long sommeTotale = (sommeVie != null ? sommeVie : 0) + (sommeIARD != null ? sommeIARD : 0);
    //     // response.put("somme_totale", sommeTotale);

    //     // Obtenir l'année précédente
    //     int anneePrecedente = Integer.parseInt(annee) - 1;

    //     // Obtenir la somme totale des primes Vie et IARD pour le même trimestre de l'année précédente
    //     Long sommeVieAnneePrecedente = noteConjonctureVIERepository.sumPrestationVieEffectivementPayesForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     Long sommeIARDAnneePrecedente = noteConjonctureIARDRepository.sumDont_Sisnistre_PayeeForTrimestre(String.valueOf(anneePrecedente), trimestre);
    //     Long sommeTotaleAnneePrecedente = (sommeVieAnneePrecedente != null ? sommeVieAnneePrecedente : 0) + (sommeIARDAnneePrecedente != null ? sommeIARDAnneePrecedente : 0);
    //     response.put("somme_totale_annee_precedente", sommeTotaleAnneePrecedente);

    //     // Calcul du taux d'évolution
    //     double tauxEvolution = 0.0;
    //     if (sommeTotaleAnneePrecedente != null && sommeTotaleAnneePrecedente != 0) {
    //         tauxEvolution = ((double) (sommeTotale - sommeTotaleAnneePrecedente) / sommeTotaleAnneePrecedente) * 100;
    //     }
    //     response.put("taux_evolution", tauxEvolution);


    //     return response;
    // }

    }







