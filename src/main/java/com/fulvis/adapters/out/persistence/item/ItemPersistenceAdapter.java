package com.fulvis.adapters.out.persistence.item;

import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.domain.item.Item;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ItemPersistenceAdapter implements ItemRepositoryPort {

    private final SpringDataItemJpaRepository repository;

    public ItemPersistenceAdapter(SpringDataItemJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Optional<Item> findById(String itemId) {
        return repository.findById(itemId)
                .map(ItemJpaEntity::toDomain);
    }

    @Override
    public List<Item> findAllByIdsForUpdate(Collection<String> itemIds) {
        return repository.findAllByIdsForUpdate(itemIds).stream()
                .map(ItemJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void saveAll(Collection<Item> items) {
        List<ItemJpaEntity> entities = items.stream()
                .map(this::toEntity)
                .toList();
        repository.saveAll(entities);
    }

    private ItemJpaEntity toEntity(Item item) {
        return repository.findById(item.id())
                .map(entity -> {
                    entity.updateFrom(item);
                    return entity;
                })
                .orElseGet(() -> ItemJpaEntity.from(item));
    }
}
