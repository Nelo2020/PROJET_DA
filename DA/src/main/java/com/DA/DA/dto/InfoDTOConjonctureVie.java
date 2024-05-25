package com.DA.DA.dto;

import java.util.Date;

public record InfoDTOConjonctureVie(
                                    Date date_soumission,
                                    String annee,
                                    String trimestre,


                                    Long nombre_contrat_assurance,
                                    Double prime_emise_nette_annulations,

                                    Double assurance_individuelle_prime,
                                    Double contrats_cas_vie_prime_emise,
                                    Double contrats_cas_deces_prime_emise,

                                    Double contrats_mixtes_prime_emise,
                                    Double capitalisation_et_epargne_prime_emise,
                                    Double complementaire_prime_emise,
                                    Double assurance_collective_prime,
                                    Double contrats_cas_vie_collective_prime_emise,
                                    Double contrats_cas_deces_collective_prime_emise,
                                    Double contrats_mixtes_collective_prime_emise,
                                    Double capitalisation_et_epargne_collective_prime_emise,
                                    Double complementaire_collective_prime_emise,

                                    Double microassurance_vie_prime_emise,
                                    Double acceptations_vie,
                                    Double primes_cedees_en_reassurance,
                                    Double provisions_mathematiques,
                                    Double stock_de_sinistres_et_capitaux_echus,
                                    Double prestations_vie_effectivement_payees,
                                    Double assurances_individuelles,
                                    Double contrats_en_cas_de_vie_individuelles_prestation_vie,
                                    Double contrats_en_cas_de_deces_individuelles_prestation_vie,
                                    Double contrats_mixtes_individuelles_prestation_vie,
                                    Double capitalisation_et_epargne_individuelles_prestation_vie,
                                    Double complementaires_individuelles_prestation_vie,

                                    Double assurances_collectives,
                                    Double contrats_en_cas_de_vie_collectives_prestation_vie,
                                    Double contrats_en_cas_de_deces_collectives_prestation_vie,
                                    Double contrats_mixtes_collectives_prestation_vie,
                                    Double capitalisation_et_epargne_collectives_prestation_vie,
                                    Double complementaires_collectives_prestation_vie,

                                    Double microassurance_vie_prestation_vie,
                                    Double production_des_courtiers,
                                    Double commissions_servies_courtiers,
                                    Double production_des_autres_intermediaires,
                                    Double commissions_servies_autres_intermediaires,
                                    Double autres_charges,
                                    Double dont_frais_du_personnel,
                                    Long nombre_hommes,
                                    Long nombre_femmes,
                                    Long effectif_du_personnel,

                                    Double creances_sur_etat,
                                    Double prime_impayee,
                                    Double emprunts_publics_echus,


                                    String nom_societe,
                                    com.DA.DA.entite.Type_AssuranceE type_assurance,
                                    com.DA.DA.entite.StatutE statut,
                                    String nom_membre,
                                    String prenoms_membre,
                                    String genre_membre,
                                    String poste_membre,
                                    String email_membre

) {
}
