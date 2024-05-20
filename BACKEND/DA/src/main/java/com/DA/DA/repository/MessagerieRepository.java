package com.DA.DA.repository;

import com.DA.DA.entite.Da;
import com.DA.DA.entite.Messagerie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessagerieRepository extends CrudRepository<Messagerie, Long> {
}
