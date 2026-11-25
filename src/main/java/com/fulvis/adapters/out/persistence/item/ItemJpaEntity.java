package com.fulvis.adapters.out.persistence.item;

import com.fulvis.domain.item.Item;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "items")
public class ItemJpaEntity {

    @Id
    @Column(name = "item_id", nullable = false, updatable = false)
    private String id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "stock_total", nullable = false)
    private int stockTotal;

    @Column(name = "stock_reserved", nullable = false)
    private int stockReserved;

    protected ItemJpaEntity() {
    }

    private ItemJpaEntity(Item item) {
        this.id = item.id();
        updateFrom(item);
    }

    public static ItemJpaEntity from(Item item) {
        return new ItemJpaEntity(item);
    }

    public void updateFrom(Item item) {
        this.reference = item.reference();
        this.name = item.name();
        this.stockTotal = item.stockTotal();
        this.stockReserved = item.stockReserved();
    }

    public Item toDomain() {
        return new Item(id, reference, name, stockTotal, stockReserved);
    }
}
