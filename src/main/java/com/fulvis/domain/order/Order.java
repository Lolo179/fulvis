package com.fulvis.domain.order;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Order {

    private final String id;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;
    private final List<OrderLine> lines;
    private final List<OrderHistory> history;

    private Order(String id, OrderStatus status, Instant createdAt, Instant updatedAt, List<OrderLine> lines, List<OrderHistory> history) {
        this.id = requireText(id, "id");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.lines = validateLines(lines);
        this.history = new ArrayList<>(Objects.requireNonNull(history, "history must not be null"));
    }

    public static Order create(String id, List<OrderLine> lines, Instant createdAt) {
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        return new Order(
                id,
                OrderStatus.MANAGED,
                createdAt,
                createdAt,
                lines,
                List.of(new OrderHistory(null, OrderStatus.MANAGED, createdAt))
        );
    }

    public void ensureCanApply(OrderTransitionRule rule) {
        Objects.requireNonNull(rule, "rule must not be null");
        ensureHasLines();
        if (rule.from() != status) {
            throw new InvalidOrderTransitionException("Transition rule starts from " + rule.from() + " but order status is " + status);
        }
    }

    public void applyTransition(OrderTransitionRule rule, Instant occurredAt) {
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        ensureCanApply(rule);

        OrderStatus previousStatus = status;
        status = rule.to();
        updatedAt = occurredAt;
        history.add(new OrderHistory(previousStatus, status, occurredAt));
    }

    public String id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    public List<OrderHistory> history() {
        return List.copyOf(history);
    }

    private void ensureHasLines() {
        if (lines.isEmpty()) {
            throw new InvalidOrderLinesException("Order must contain at least one line");
        }
    }

    private static List<OrderLine> validateLines(List<OrderLine> lines) {
        Objects.requireNonNull(lines, "lines must not be null");
        if (lines.isEmpty()) {
            throw new InvalidOrderLinesException("Order must contain at least one line");
        }

        Set<String> itemIds = new HashSet<>();
        List<OrderLine> validatedLines = new ArrayList<>();
        for (OrderLine line : lines) {
            OrderLine validatedLine = Objects.requireNonNull(line, "line must not be null");
            if (!itemIds.add(validatedLine.itemId())) {
                throw new InvalidOrderLinesException("Order cannot contain duplicated itemId: " + validatedLine.itemId());
            }
            validatedLines.add(validatedLine);
        }
        return validatedLines;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
