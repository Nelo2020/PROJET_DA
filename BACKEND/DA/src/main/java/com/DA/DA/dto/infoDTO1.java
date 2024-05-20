package com.DA.DA.dto;

import com.DA.DA.entite.Type_AssuranceE;

public record infoDTO1(Long id_societe,String nom, String dirigeant, String siege, String contact,String num_agrement, String email, Type_AssuranceE type_assurance,com.DA.DA.entite.TypeRoles role) {

}

