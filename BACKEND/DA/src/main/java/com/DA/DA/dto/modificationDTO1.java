package com.DA.DA.dto;

import com.DA.DA.entite.Role;
import com.DA.DA.entite.TypeRoles;

public record modificationDTO1(String email, String motpasse, Role role, String nouveauMotDePasse,String confirmerNouveauMotDePasse) {
}

