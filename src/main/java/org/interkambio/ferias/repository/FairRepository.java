package org.interkambio.ferias.repository;

import org.interkambio.ferias.entity.Fair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface FairRepository extends JpaRepository<Fair, Long> {

    @Query("SELECT f FROM Fair f LEFT JOIN FETCH f.dispatchItems WHERE f.id = :id")
    Optional<Fair> findByIdWithDispatchItems(@Param("id") Long id);
}