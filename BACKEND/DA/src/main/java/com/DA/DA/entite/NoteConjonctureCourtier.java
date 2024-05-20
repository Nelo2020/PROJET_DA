package com.DA.DA.entite;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "NOTE_CONJONCTURE_COURTIER")
public class NoteConjonctureCourtier implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_NC_COURTIER_TEMPORAIRE")
    private long id_NC_COURTIER;
    @Column(name = "annee")
    private String annee;
    @Column(name = "trimestre")
    private String trimestre;
    @Temporal(TemporalType.DATE)
    @Column(name = "date_soumission", nullable = false, updatable = false)
    @JsonFormat(pattern="dd/MM/yyyy")
    private Date dateSoumission;
    //infos general
    private String societe;
    private String siege;
    private String gerant;
    private String telephone;
    private String email;
    private String numero_RCCM;
    //autres infos
    private Long bureaux_secondaire;
    private Long abidjan;
    private Long interieur;
    private Long societe_partenaire;
    private Long compte_courant;
    private Long nombre_hommes;
    private Long nombre_femmes;
    private Long total_personnel;

    //activités de l'entreprise
    private Double emissions_primes;
    private Double commissions_encaissees;
    private Double total_charge_ou_frais_generaux;
    private Double montant_fonds_geres;
    private Double montant_honoraires;




    @PrePersist
    public void onPrePersist() {

        calculerValeurs(); // Calculez également les autres valeurs
    }
    @PreUpdate
    public void onPreUpdate() {
        calculerValeurs(); // Met à jour les valeurs calculées lors de chaque mise à jour de l'entité
    }

    public void calculerValeurs() {
        this.societe=getMembreAssurance().getSociete().getNom();
        this.siege=getMembreAssurance().getSociete().getSiege();
        this.gerant=getMembreAssurance().getSociete().getDirigeant();
        this.telephone=getMembreAssurance().getSociete().getContact();
        this.email=getMembreAssurance().getSociete().getEmail();
        this.numero_RCCM=getMembreAssurance().getSociete().getNum_agrement();
        this.total_personnel=nombre_hommes + nombre_femmes;
        this.bureaux_secondaire=abidjan+interieur;
    }
    // relation avec les tables DA,Compagnie_assurance,Statut et commentaires
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
