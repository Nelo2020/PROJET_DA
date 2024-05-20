package com.DA.DA.entite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.management.relation.Relation;
import java.util.List;

@Data
@Entity
@Table(name = "type_assurance")
public class Type_Assurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_typeassurance")
    private int id_typeassurance;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Type_AssuranceE libelles;
//
//     Relation avec societe
    @OneToMany(mappedBy = "typeAssurance", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<Societes> societe;


}
