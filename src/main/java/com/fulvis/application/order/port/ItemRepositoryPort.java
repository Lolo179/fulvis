package com.fulvis.application.order.port;

import com.fulvis.domain.item.Item;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ItemRepositoryPort {

    Optional<Item> findById(String itemId);

    List<Item> findAllByIdsForUpdate(Collection<String> itemIds);

    void saveAll(Collection<Item> items);
}
