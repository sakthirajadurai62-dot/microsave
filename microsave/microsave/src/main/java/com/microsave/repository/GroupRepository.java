package com.microsave.repository;

import com.microsave.entity.SelfHelpGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<SelfHelpGroup, Long> {
    boolean existsByNameIgnoreCase(String name);

    // Row lock: two simultaneous loan requests cannot both drain the same pool.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from SelfHelpGroup g where g.id = :id")
    Optional<SelfHelpGroup> findByIdForUpdate(@Param("id") Long id);
}
