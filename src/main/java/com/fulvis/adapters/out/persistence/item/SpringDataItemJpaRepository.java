package com.fulvis.adapters.out.persistence.item;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SpringDataItemJpaRepository extends JpaRepository<ItemJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from ItemJpaEntity item where item.id in :itemIds order by item.id")
    List<ItemJpaEntity> findAllByIdsForUpdate(@Param("itemIds") Collection<String> itemIds);
}
