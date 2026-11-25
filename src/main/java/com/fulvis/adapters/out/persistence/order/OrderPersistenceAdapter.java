package com.fulvis.adapters.out.persistence.order;

import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.order.Order;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderJpaRepository repository;

    public OrderPersistenceAdapter(SpringDataOrderJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return repository.findById(orderId)
                .map(OrderJpaEntity::toDomain);
    }

    @Override
    public void requireCurrentVersion(Order order) {
        if (repository.findVersionById(order.id()).filter(version -> version.equals(order.version())).isEmpty()) {
            throw new ConcurrencyConflictException("Order version conflict for orderId " + order.id());
        }
    }

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        if (order.version() == null) {
            repository.save(OrderJpaEntity.from(order));
            return;
        }

        OrderJpaEntity entity = repository.findById(order.id())
                .orElseThrow(() -> new ConcurrencyConflictException("Order version conflict for orderId " + order.id()));
        entity.updateFrom(order);
        repository.save(entity);
    }
}
