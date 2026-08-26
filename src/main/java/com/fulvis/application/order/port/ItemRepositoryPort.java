package com.fulvis.application.order.port;

import com.fulvis.domain.item.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepositoryPort {

    List<Item> findAllByIdsForUpdate(Collection<String> itemIds);

    void saveAll(Collection<Item> items);
}
