package com.DA.DA.entite;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Data
@Entity
@Table(name = "EMPLOYEES_DA")
public class Da implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_DA")
    private Long id;

    private String nom;
    private String prenoms;
    private String genre;
    private String email;
    private String username;
    private String motpasse;
    private String poste;
    private String telephone;
    private Boolean actif;
    public Da() {
        // Constructeur par défaut nécessaire pour la désérialisation depuis JSON
    }
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Societes> societe;

    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MembreAssurance> membreAssurance;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Messagerie> messageries;

    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NoteConjonctureCourtierTemporaire> noteConjonctureCourtierTemporaires;
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note_ConjonctureVie_temporaire> noteConjonctureVieTemporaires;
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NoteConjonctureIARDTemporaire> noteConjonctureIARDTemporaires;
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NoteConjonctureCourtier> noteConjonctureCourtiers;
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note_ConjonctureIARD> noteConjonctureIARDS;
    @OneToMany(mappedBy = "da", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Note_ConjonctureVIE> noteConjonctureVIES;

    // Getters and setters

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.role.getLibelle().name()));
    }



    @Override
    public String getPassword() {
        return motpasse; // Renvoie le mot de passe
    }

    @Override
    public String getUsername() {
        return username; // Renvoie le nom d'utilisateur
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Peut être implémenté en fonction de la logique de votre application
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Peut être implémenté en fonction de la logique de votre application
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Peut être implémenté en fonction de la logique de votre application
    }

    @Override
    public boolean isEnabled() {
        return actif; // Renvoie l'état d'activation de l'utilisateur
    }
}
