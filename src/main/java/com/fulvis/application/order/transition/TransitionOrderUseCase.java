package com.fulvis.application.order.transition;

import com.fulvis.application.error.ConcurrencyConflictException;
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
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    private final TransactionTemplate writeTransaction;
    private final TransactionTemplate readTransaction;

    public TransitionOrderUseCase(
            OrderRepositoryPort orderRepository,
            ItemRepositoryPort itemRepository,
            OrderStateMachine stateMachine,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository must not be null");
        this.stateMachine = Objects.requireNonNull(stateMachine, "stateMachine must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
        this.writeTransaction = new TransactionTemplate(transactionManager);
        writeTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        writeTransaction.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.readTransaction = new TransactionTemplate(transactionManager);
        readTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        readTransaction.setReadOnly(true);
    }

    public TransitionOrderResult execute(TransitionOrderCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // The template completes commit or rollback before recovery can read the order again.
        try {
            return writeTransaction.execute(transaction -> transition(command));
        } catch (ConcurrencyConflictException | OptimisticLockingFailureException conflict) {
            return readTransaction.execute(transaction -> resolveConflict(command, conflict));
        }
    }

    private TransitionOrderResult transition(TransitionOrderCommand command) {
        Order order = loadOrder(command.orderId());
        OrderTransitionResolution resolution = stateMachine.resolve(order.status(), command.event());

        return switch (resolution.type()) {
            case ALREADY_SATISFIED -> alreadySatisfied(order);
            case INVALID -> throw new InvalidOrderTransitionException("Invalid transition event " + command.event() + " for status " + order.status());
            case APPLICABLE -> applyTransition(order, resolution.rule());
        };
    }

    private TransitionOrderResult resolveConflict(TransitionOrderCommand command, RuntimeException conflict) {
        Order current = loadOrder(command.orderId());
        if (stateMachine.resolve(current.status(), command.event()).type()
                == OrderTransitionResolution.Type.ALREADY_SATISFIED) {
            return alreadySatisfied(current);
        }
        throw new ConcurrencyConflictException("Order version conflict for orderId " + command.orderId(), conflict);
    }

    private Order loadOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private static TransitionOrderResult alreadySatisfied(Order order) {
        return new TransitionOrderResult(order.id(), order.status(), order.updatedAt(), true);
    }

    private TransitionOrderResult applyTransition(Order order, OrderTransitionRule rule) {
        order.ensureCanApply(rule);

        Collection<Item> changedItems = applyStockActionIfNeeded(order, rule.requiredStockAction());

        // PostgreSQL timestamps have microsecond precision, including on a later retry.
        Instant occurredAt = Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
        order.applyTransition(rule, occurredAt);
        orderRepository.save(order);
        if (!changedItems.isEmpty()) {
            itemRepository.saveAll(changedItems);
        }

        return new TransitionOrderResult(order.id(), order.status(), occurredAt, false);
    }

    private Collection<Item> applyStockActionIfNeeded(Order order, StockAction stockAction) {
        if (stockAction == StockAction.NONE) {
            return List.of();
        }

        Map<String, Item> itemsById = loadItemsById(order.lines());
        orderRepository.requireCurrentVersion(order);
        for (OrderLine line : order.lines()) {
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
