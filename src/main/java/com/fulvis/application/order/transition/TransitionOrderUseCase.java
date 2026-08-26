package com.fulvis.application.order.transition;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.OrderNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.item.Item;
import com.fulvis.domain.order.InvalidOrderTransitionException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStateMachine;
import com.fulvis.domain.order.OrderStateMachine.OrderTransitionResolution;
import com.fulvis.domain.order.OrderTransitionRule;
import com.fulvis.domain.order.StockAction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TransitionOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ItemRepositoryPort itemRepository;
    private final OrderStateMachine stateMachine;
    private final Clock clock;

    public TransitionOrderUseCase(
            OrderRepositoryPort orderRepository,
            ItemRepositoryPort itemRepository,
            OrderStateMachine stateMachine,
            Clock clock
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository must not be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public TransitionOrderResult execute(TransitionOrderCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
        Instant occurredAt = Instant.now(clock);
        OrderTransitionResolution resolution = stateMachine.resolve(order.status(), command.event());

        return switch (resolution.type()) {
            case ALREADY_SATISFIED -> new TransitionOrderResult(order.id(), order.status(), occurredAt, true);
            case INVALID -> throw new InvalidOrderTransitionException("Invalid transition event " + command.event() + " for status " + order.status());
            case APPLICABLE -> applyTransition(order, resolution.rule(), occurredAt);
        };
    }

    private TransitionOrderResult applyTransition(Order order, OrderTransitionRule rule, Instant occurredAt) {
        order.ensureCanApply(rule);

        Collection<Item> changedItems = applyStockActionIfNeeded(order.lines(), rule.requiredStockAction());

        order.applyTransition(rule, occurredAt);
        orderRepository.save(order);
        if (!changedItems.isEmpty()) {
            itemRepository.saveAll(changedItems);
        }

        return new TransitionOrderResult(order.id(), order.status(), occurredAt, false);
    }

    private Collection<Item> applyStockActionIfNeeded(List<OrderLine> lines, StockAction stockAction) {
        if (stockAction == StockAction.NONE) {
            return List.of();
        }

        Map<String, Item> itemsById = loadItemsById(lines);
        for (OrderLine line : lines) {
            Item item = itemsById.get(line.itemId());
            switch (stockAction) {
                case RELEASE -> item.release(line.quantity());
                case DECREMENT -> item.decrement(line.quantity());
                case NONE -> {
                }
                case RESERVE -> throw new IllegalArgumentException("RESERVE is not a transition stock action");
            }
        }
        return itemsById.values();
    }

    private Map<String, Item> loadItemsById(List<OrderLine> lines) {
        List<String> itemIds = lines.stream()
                .map(OrderLine::itemId)
                .toList();
        Map<String, Item> itemsById = itemRepository.findAllByIdsForUpdate(itemIds).stream()
                .collect(Collectors.toMap(Item::id, Function.identity(), (first, second) -> first, LinkedHashMap::new));

        for (String itemId : itemIds) {
            if (!itemsById.containsKey(itemId)) {
                throw new ItemNotFoundException(itemId);
            }
        }
        return itemsById;
    }
}
