package com.DA.DA.repository;

import com.DA.DA.entite.Role;
import com.DA.DA.entite.Statut;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatutRepository extends CrudRepository<Statut, Long> {
}
