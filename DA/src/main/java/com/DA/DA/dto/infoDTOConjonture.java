package com.DA.DA.dto;

import java.util.Date;

public record infoDTOConjonture(


        Date date_soumission,
        String annee,
        String trimestre,
        Long nombre_de_contrats_assurance,

        Double prime_emise_nette_annulations,
        Double maladie_prime_emise,
        Double accidents_corporels_prime_emise,
        Double rc_automobile_prime_emise,
        Double autres_risques_automobiles_prime_emise,
        Double incendie_et_autres_dommages_aux_biens_prime_emise,
        Double credit_prime_emise,
        Double caution_prime_emise,
        Double assurance_importation_des_marchandises_prime_emise,
        Double microassurance_prime_emise,
        Double assurances_indicielles_prime_emise,
        Double autres_risques_dommages_prime_emise,
        Double acceptations_dommages,
        Double prime_cedee_en_reassurance,
        Double sinistre_a_payees,
        Double stock_de_sinistres_bons_a_payer,
        Double maladie_stock,
        Double accidents_corporels_stock,
        Double rc_automobile_stock,
        Double autres_risques_automobiles_stock,
        Double incendie_et_autres_dommages_aux_biens_stock,
        Double credit_stock,
        Double caution_stock,
        Double assurance_importation_des_marchandises_stock,
        Double microassurance_stock,
        Double assurances_indicielles_stock,
        Double autres_risques_dommages_stock,

        Double dont_sisnistre_payee,
        Double recours_encaisser,
        Double recours_effectivement_encaisser,
        Double production_des_courtiers,
        Double commissions_servies_aux_courtiers,
        Double production_autres_intermediaires,
        Double commissions_servies_aux_autres_intermediaires,

        Double creances_sur_etat,
        Double prime_impayee,
        Double emprunts_public_echus,
        Double frais_du_personnel,
        Long nombre_hommes,
        Long nombre_femmes,
        Long effectif_du_personnel,
        Double autres_charges,

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
