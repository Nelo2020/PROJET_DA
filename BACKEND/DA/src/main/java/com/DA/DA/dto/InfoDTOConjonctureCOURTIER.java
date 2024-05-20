package com.DA.DA.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.util.Date;

public record InfoDTOConjonctureCOURTIER(
                                         Date dateSoumission,
                                         String annee,
                                         String trimestre,
                                         //infos general
                                         String societe,
                                         String siege,
                                         String gerant,
                                         String telephone,
                                         String email,
                                         String numero_RCCM,
                                         //autres infos
                                         Long bureaux_secondaire,
                                         Long abidjan,
                                         Long interieur,
                                         Long societe_partenaire,
                                         Long compte_courant,
                                         Long nombre_hommes,
                                         Long nombre_femmes,
                                         Long total_personnel,
                                         //activités de l'entreprise
                                         Double emissions_primes,
                                         Double commissions_encaissees,
                                         Double total_charge_ou_frais_generaux,
                                         Double montant_fonds_geres,
                                         Double montant_honoraires,
                                         com.DA.DA.entite.Type_AssuranceE type_assurance,
                                         com.DA.DA.entite.StatutE statut,
                                         String nom_membre,
                                         String prenoms_membre,
                                         String genre_membre,
                                         String poste_membre,
                                         String email_membre
) {
}
