package com.DA.DA.repository;



import com.DA.DA.entite.Da;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@EnableJpaRepositories
@Repository
public interface DaRepository extends CrudRepository<Da, Long> {


    Optional<Da> findById(Long id_DA);


    Da findByEmail(String email);



}

