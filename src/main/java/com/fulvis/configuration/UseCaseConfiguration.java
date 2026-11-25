package com.fulvis.configuration;

import com.fulvis.application.item.stock.GetItemStockUseCase;
import com.fulvis.application.order.create.CreateOrderUseCase;
import com.fulvis.application.order.get.GetOrderUseCase;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.application.order.transition.TransitionOrderUseCase;
import com.fulvis.domain.order.OrderStateMachine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfiguration {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    OrderStateMachine orderStateMachine() {
        return new OrderStateMachine();
    }

    @Bean
    CreateOrderUseCase createOrderUseCase(OrderRepositoryPort orders, ItemRepositoryPort items, Clock clock) {
        return new CreateOrderUseCase(orders, items, clock);
    }

    @Bean
    TransitionOrderUseCase transitionOrderUseCase(
            OrderRepositoryPort orders,
            ItemRepositoryPort items,
            OrderStateMachine stateMachine,
            Clock clock,
            PlatformTransactionManager transactionManager
    ) {
        return new TransitionOrderUseCase(orders, items, stateMachine, clock, transactionManager);
    }

    @Bean
    GetOrderUseCase getOrderUseCase(OrderRepositoryPort orders) {
        return new GetOrderUseCase(orders);
    }

    @Bean
    GetItemStockUseCase getItemStockUseCase(ItemRepositoryPort items) {
        return new GetItemStockUseCase(items);
    }
}
