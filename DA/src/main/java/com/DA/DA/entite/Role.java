package com.DA.DA.entite;

import com.DA.DA.entite.TypeRoles;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Entity
@Table(name = "role")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TypeRoles libelle;

//    // Relation avec la classe Compagnie_Assurance
    @OneToMany(mappedBy = "roles", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Societes> societe;

    // Relation avec la classe Da
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Da> da;

    // Autres attributs, constructeurs, getters et setters
}
