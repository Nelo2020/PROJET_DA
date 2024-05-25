package com.DA.DA.repository;

import com.DA.DA.entite.Da;
import com.DA.DA.entite.Type_Assurance;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@EnableJpaRepositories
@Repository
public interface Type_assuranceRepository  extends CrudRepository<Type_Assurance, Long> {
}
