package com.DA.DA.repository;


import com.DA.DA.entite.Da;
import com.DA.DA.entite.MembreAssurance;
import com.DA.DA.entite.Societes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembreAssuranceRepository extends JpaRepository<MembreAssurance, Long> {



    Optional<MembreAssurance> findByEmail(String email);

    Optional<MembreAssurance> findByNom(String nom);

    Optional<MembreAssurance> findById(Long id_compagnie);

    @Query("SELECT ma.societe.id_societe FROM MembreAssurance ma WHERE ma.id_Membre = :membreId")
    Optional<Long> findSocieteIdByMembreId(@Param("membreId") Long membreId);

    @Query("SELECT ma.societe FROM MembreAssurance ma WHERE ma.id_Membre = :membreId")
    Optional<Societes> getSocieteByMembreAssurance(@Param("membreId") Long membreId);



}

