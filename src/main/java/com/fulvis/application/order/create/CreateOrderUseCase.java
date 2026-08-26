package com.fulvis.application.order.create;

import com.fulvis.application.order.ItemNotFoundException;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.domain.item.Item;
import com.fulvis.domain.order.InvalidOrderLinesException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderLine;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final ItemRepositoryPort itemRepository;
    private final Clock clock;

    public CreateOrderUseCase(OrderRepositoryPort orderRepository, ItemRepositoryPort itemRepository, Clock clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository, "orderRepository must not be null");
        this.itemRepository = Objects.requireNonNull(itemRepository, "itemRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public CreateOrderResult execute(CreateOrderCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        List<OrderLine> lines = toOrderLines(command.lines());
        validateNoDuplicatedItemIds(lines);

        Map<String, Item> itemsById = loadItemsById(lines);
        for (OrderLine line : lines) {
            itemsById.get(line.itemId()).reserve(line.quantity());
        }

        Instant createdAt = Instant.now(clock);
        Order order = Order.create(UUID.randomUUID().toString(), lines, createdAt);

        orderRepository.save(order);
        itemRepository.saveAll(itemsById.values());

        return new CreateOrderResult(order.id(), order.status(), createdAt);
    }

    private static List<OrderLine> toOrderLines(List<CreateOrderLineCommand> lineCommands) {
        if (lineCommands.isEmpty()) {
            throw new InvalidOrderLinesException("Order must contain at least one line");
        }
        return lineCommands.stream()
                .map(line -> new OrderLine(line.itemId(), line.quantity()))
                .toList();
    }

    private static void validateNoDuplicatedItemIds(List<OrderLine> lines) {
        Set<String> itemIds = new HashSet<>();
        for (OrderLine line : lines) {
            if (!itemIds.add(line.itemId())) {
                throw new InvalidOrderLinesException("Order cannot contain duplicated itemId: " + line.itemId());
            }
        }
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
