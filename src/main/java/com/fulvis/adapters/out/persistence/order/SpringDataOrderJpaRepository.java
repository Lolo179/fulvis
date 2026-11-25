package com.fulvis.adapters.out.persistence.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataOrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    @Override
    @EntityGraph(attributePaths = {"lines", "history"})
    Optional<OrderJpaEntity> findById(String orderId);

    @Query("select o.version from OrderJpaEntity o where o.id = :orderId")
    Optional<Long> findVersionById(@Param("orderId") String orderId);
}
