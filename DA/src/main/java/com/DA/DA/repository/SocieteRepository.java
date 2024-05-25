package com.DA.DA.repository;

import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import com.DA.DA.entite.Type_AssuranceE;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@EnableJpaRepositories
@Repository
public interface SocieteRepository extends CrudRepository<Societes,Long> {
    Optional<Societes> findByEmail(String email);

    Optional<Societes> findByNom(String nom);

    Optional<Societes> findById(Long id_compagnie);



}

