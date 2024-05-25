package com.DA.DA.entite;//package com.DA.DA.entites;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "STATUT")
public class Statut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Long idStatut;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutE etat;

    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Note_ConjonctureIARD> note_conjonctureIARD;
    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Note_ConjonctureVIE> note_conjonctureVIES;
    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Note_ConjonctureVie_temporaire> noteConjonctureVieTemporaires;
    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NoteConjonctureIARDTemporaire> noteConjonctureIARDTemporaires;
    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NoteConjonctureCourtier> noteConjonctureCourtiers;
    @OneToMany(mappedBy = "statut", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NoteConjonctureCourtierTemporaire> noteConjonctureCourtierTemporaires;

    // Ajoutez les autres attributs, constructeurs, getters et setters
}
