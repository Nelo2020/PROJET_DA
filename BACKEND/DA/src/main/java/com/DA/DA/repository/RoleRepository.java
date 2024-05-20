package com.DA.DA.repository;
import com.DA.DA.entite.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {

    Optional<Role> findById(Long roleId);
}
