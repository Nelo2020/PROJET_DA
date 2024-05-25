package com.DA.DA.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "NOTE_CONJONCTURE_IARD_TEMPORAIRE")

public class NoteConjonctureIARDTemporaire implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_NC_IARD")
    private long id_NC_IARD;
    private String annee;
    private String trimestre;
    @Temporal(TemporalType.DATE)
    @Column(name = "date_soumission", nullable = false, updatable = false)
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateSoumission;

    private Long nombre_de_contrats_assurance;

    private Double prime_emise_nette_annulations;
    private Double maladie_prime_emise;
    private Double accidents_corporels_prime_emise;
    private Double rc_automobile_prime_emise;
    private Double autres_risques_automobiles_prime_emise;
    private Double incendie_et_autres_dommages_aux_biens_prime_emise;
    private Double credit_prime_emise;
    private Double caution_prime_emise;
    private Double assurance_importation_des_marchandises_prime_emise;
    private Double microassurance_prime_emise;
    private Double assurances_indicielles_prime_emise;
    private Double autres_risques_dommages_prime_emise;
    private Double acceptations_dommages;

    private Double prime_cedee_en_reassurance;
    private Double sinistre_a_payees;
    private Double stock_de_sinistres_bons_a_payer;
    private Double dont_sisnistre_payee;
    private Double recours_encaisser;
    private Double recours_effectivement_encaisser;

    private Double maladie_stock;
    private Double accidents_corporels_stock;
    private Double rc_automobile_stock;
    private Double autres_risques_automobiles_stock;
    private Double incendie_et_autres_dommages_aux_biens_stock;
    private Double credit_stock;
    private Double caution_stock;
    private Double assurance_importation_des_marchandises_stock;
    private Double microassurance_stock;
    private Double assurances_indicielles_stock;
    private Double autres_risques_dommages_stock;

    private Double production_des_courtiers;
    private Double commissions_servies_aux_courtiers;
    private Double production_autres_intermediaires;
    private Double commissions_servies_aux_autres_intermediaires;

    private Double creances_sur_etat;
    private Double prime_impayee;
    private Double emprunts_public_echus;
    private Double frais_du_personnel;
    private Long nombre_hommes;
    private Long nombre_femmes;
    private Long effectif_du_personnel;
    private Double autres_charges;

    @PrePersist
    public void onPrePersist() {
        if (dateSoumission == null) {
            setDateSoumission(new Date());
        }
        calculerValeurs();
    }

    @PreUpdate
    public void onPreUpdate() {
        calculerValeurs(); // Met à jour les valeurs calculées lors de chaque mise à jour de l'entité
    }

    // Méthodes de calcul pour creances_sur_etat, stock_de_sinistres_bons_a_payer, et prime_emisse_nette_annulation...
    public void calculerValeurs() {
            this.creances_sur_etat = prime_impayee + emprunts_public_echus;
            this.stock_de_sinistres_bons_a_payer = maladie_stock + accidents_corporels_stock + rc_automobile_stock + autres_risques_automobiles_stock + incendie_et_autres_dommages_aux_biens_stock +
                    credit_stock + caution_stock + assurance_importation_des_marchandises_stock + assurances_indicielles_stock + autres_risques_dommages_stock + microassurance_stock;
            this.prime_emise_nette_annulations = maladie_prime_emise + rc_automobile_prime_emise + autres_risques_automobiles_prime_emise + accidents_corporels_prime_emise +
                    incendie_et_autres_dommages_aux_biens_prime_emise + credit_prime_emise + caution_prime_emise + assurances_indicielles_prime_emise + autres_risques_dommages_prime_emise + microassurance_prime_emise;
            this.effectif_du_personnel = nombre_hommes + nombre_femmes;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_DA", nullable = false)
    private Da da;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_membre", nullable = false)
    private MembreAssurance membreAssurance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_message")
    private Messagerie messagerie;
}
