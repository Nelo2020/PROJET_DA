package com.DA.DA.entite;//package com.DA.DA.entites;

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
@Table(name = "Societe")
public class Societes  implements Serializable, UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_societe;

    private String nom;
    private String dirigeant;
    private Boolean actif = false;
    private String siege;
    private String contact;
    private String num_agrement;
    private String email;


    @ManyToOne
    @JoinColumn(name = "id_da") // Assurez-vous que le nom de la colonne correspond à votre schéma de base de données
    private Da da;

    @ManyToOne
    @JoinColumn(name = "id_role")
    private Role roles;

    @OneToMany(mappedBy = "societe")
    private List<MembreAssurance> membreAssurances;

    @ManyToOne
    @JoinColumn(name = "id_typeassurance")
    private Type_Assurance typeAssurance;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + this.roles.getLibelle().name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
