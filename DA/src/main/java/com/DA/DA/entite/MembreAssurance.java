package com.DA.DA.entite;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Entity
@Table(name = "Membres_assurance")
public class MembreAssurance implements Serializable, UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_membre")
    private Long id_Membre;

    private String nom;
    private String prenoms;
    private String genre;
    private String email;
    private String username;
    private String motpasse;
    private String poste;
    private String telephone;
    private Boolean actif = false;


    @ManyToOne
    @JoinColumn(name = "id_societe")
    private Societes societe;

    @ManyToOne
    @JoinColumn(name = "id_da") // Assurez-vous que le nom de la colonne correspond à votre schéma de base de données
    private Da da;






    @OneToMany(mappedBy = "membreAssurance")
    private List<Messagerie> messagesEnvoyes;

    @OneToMany(mappedBy = "membreAssurance")
    private List<Note_ConjonctureVie_temporaire> noteConjonctureVieTemporaires;

    @OneToMany(mappedBy = "membreAssurance")
    private List<NoteConjonctureIARDTemporaire> noteConjonctureIARDTemporaires;

    @OneToMany(mappedBy = "membreAssurance")
    private List<NoteConjonctureCourtierTemporaire> noteConjonctureCourtierTemporaires;

    @OneToMany(mappedBy = "membreAssurance")
    private List<NoteConjonctureCourtier> noteConjonctureCourtiers;

    @OneToMany(mappedBy = "membreAssurance")
    private List<Note_ConjonctureIARD> noteConjonctureIARDS;

    @OneToMany(mappedBy = "membreAssurance")
    private List<Note_ConjonctureVIE> noteConjonctureVIES;
//    // Getters and setters



        @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("TYPE_SOCIETE_" + this.societe.getTypeAssurance().getLibelles().name())
        );
    }
    @Override
    public String getPassword() {
        return motpasse;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return actif;
    }
}
