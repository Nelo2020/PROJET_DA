package com.DA.DA.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "NOTE_CONJONCTURE_VIE")
public class Note_ConjonctureVIE implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_NC_VIE")
    private long id_NC_VIE;
    @Column(name = "annee")
    private String annee;
    @Column(name = "trimestre")
    private String trimestre;
    @Temporal(TemporalType.DATE)
    @Column(name = "date_soumission", nullable = false)
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateSoumission;

    private Long nombre_contrat_assurance;
    private Double prime_emise_nette_annulations;

    private Double assurance_individuelle_prime;
    private Double contrats_cas_vie_prime_emise;
    private Double contrats_cas_deces_prime_emise;
    private Double contrats_mixtes_prime_emise;
    private Double capitalisation_et_epargne_prime_emise;
    private Double complementaire_prime_emise;

    private Double assurance_collective_prime;
    private Double contrats_cas_vie_collective_prime_emise;
    private Double contrats_cas_deces_collective_prime_emise;
    private Double contrats_mixtes_collective_prime_emise;
    private Double capitalisation_et_epargne_collective_prime_emise;
    private Double complementaire_collective_prime_emise;

    private Double microassurance_vie_prime_emise;
    private Double acceptations_vie;
    private Double primes_cedees_en_reassurance;
    private Double provisions_mathematiques;
    private Double stock_de_sinistres_et_capitaux_echus;

    private Double prestations_vie_effectivement_payees;
    private Double assurances_individuelles;
    private Double contrats_en_cas_de_vie_individuelles_prestation_vie;
    private Double contrats_en_cas_de_deces_individuelles_prestation_vie;
    private Double contrats_mixtes_individuelles_prestation_vie;
    private Double capitalisation_et_epargne_individuelles_prestation_vie;
    private Double complementaires_individuelles_prestation_vie;

    private Double assurances_collectives;
    private Double contrats_en_cas_de_vie_collectives_prestation_vie;
    private Double contrats_en_cas_de_deces_collectives_prestation_vie;
    private Double contrats_mixtes_collectives_prestation_vie;
    private Double capitalisation_et_epargne_collectives_prestation_vie;
    private Double complementaires_collectives_prestation_vie;

    private Double microassurance_vie_prestation_vie;
    private Double production_des_courtiers;
    private Double commissions_servies_courtiers;
    private Double production_des_autres_intermediaires;
    private Double commissions_servies_autres_intermediaires;
    private Double autres_charges;
    private Double dont_frais_du_personnel;
    private Long nombre_hommes;
    private Long nombre_femmes;
    private Long effectif_du_personnel;

    private Double creances_sur_etat;
    private Double prime_impayee;
    private Double emprunts_publics_echus;

    @PrePersist
    public void onPrePersist() {

        calculerValeurs(); // Calculez également les autres valeurs
    }
    @PreUpdate
    public void onPreUpdate() {
        calculerValeurs(); // Met à jour les valeurs calculées lors de chaque mise à jour de l'entité
    }

    public void calculerValeurs() {
        this.assurances_individuelles = contrats_en_cas_de_vie_individuelles_prestation_vie + contrats_en_cas_de_deces_individuelles_prestation_vie + contrats_mixtes_individuelles_prestation_vie + capitalisation_et_epargne_individuelles_prestation_vie
                + complementaires_individuelles_prestation_vie;
        this.assurances_collectives = contrats_en_cas_de_vie_collectives_prestation_vie + contrats_en_cas_de_deces_collectives_prestation_vie + contrats_mixtes_collectives_prestation_vie + capitalisation_et_epargne_collectives_prestation_vie
                + complementaires_collectives_prestation_vie ;
        this.prestations_vie_effectivement_payees =assurances_individuelles + assurances_collectives  + microassurance_vie_prestation_vie;

        this.assurance_individuelle_prime =  contrats_cas_vie_prime_emise +contrats_cas_deces_prime_emise + contrats_mixtes_prime_emise + capitalisation_et_epargne_prime_emise + complementaire_prime_emise;
        this.assurance_collective_prime =  contrats_cas_deces_collective_prime_emise + contrats_cas_vie_collective_prime_emise + contrats_mixtes_collective_prime_emise + capitalisation_et_epargne_collective_prime_emise + complementaire_collective_prime_emise;
        this.prime_emise_nette_annulations =  assurance_individuelle_prime+ assurance_collective_prime + microassurance_vie_prime_emise + acceptations_vie;
        this.creances_sur_etat= prime_impayee + emprunts_publics_echus;
        this.effectif_du_personnel = nombre_hommes + nombre_femmes;
    }




    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_DA", nullable = false)
    private Da da;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_membre", nullable = false)
    private MembreAssurance membreAssurance;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statut_id")
    private Statut statut;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_message")
    private Messagerie messagerie;



}