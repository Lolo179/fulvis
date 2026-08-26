package com.fulvis.application.order.create;

import java.util.List;
import java.util.Objects;

public record CreateOrderCommand(List<CreateOrderLineCommand> lines) {

    public CreateOrderCommand {
        Objects.requireNonNull(lines, "lines must not be null");
        lines = List.copyOf(lines);
    }
}
