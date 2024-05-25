package com.DA.DA.entite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "Messagerie")
public class Messagerie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_message")
    private Long id;

    private String motif;

    @ManyToOne
    @JoinColumn(name = "id_DA")
    @JsonIgnore // Ignorer la sérialisation de la relation Da pour éviter la récursion infinie
    private Da da;
    @OneToMany(mappedBy = "messagerie", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Note_ConjonctureVie_temporaire> noteConjonctureVieTemporaires;

    @OneToMany(mappedBy = "messagerie", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NoteConjonctureIARDTemporaire> noteConjonctureIARDTemporaires;

    @OneToMany(mappedBy = "messagerie", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<NoteConjonctureCourtierTemporaire> noteConjonctureCourtierTemporaires;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_membre", nullable = false)
    private MembreAssurance membreAssurance;


}
