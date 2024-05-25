package com.DA.DA.dto;

import com.DA.DA.entite.Type_Assurance;
import com.DA.DA.entite.Type_AssuranceE;

public record InfoDTOMembreAssurance(
        String nom,
        String prenoms,
        String genre,
        String email,
        String username,
        String poste,
        String nom_societe,
        Type_AssuranceE typeAssurance,
        String telephone) {
}

