package com.fulvis.adapters.out.persistence;

import com.fulvis.FulvisApplication;
import com.fulvis.application.error.ConcurrencyConflictException;
import com.fulvis.application.order.create.CreateOrderCommand;
import com.fulvis.application.order.create.CreateOrderLineCommand;
import com.fulvis.application.order.create.CreateOrderUseCase;
import com.fulvis.application.order.port.ItemRepositoryPort;
import com.fulvis.application.order.port.OrderRepositoryPort;
import com.fulvis.application.order.transition.TransitionOrderCommand;
import com.fulvis.application.order.transition.TransitionOrderResult;
import com.fulvis.application.order.transition.TransitionOrderUseCase;
import com.fulvis.domain.item.InsufficientStockException;
import com.fulvis.domain.order.Order;
import com.fulvis.domain.order.OrderEvent;
import com.fulvis.domain.order.OrderHistory;
import com.fulvis.domain.order.OrderLine;
import com.fulvis.domain.order.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = FulvisApplication.class)
@Import(PersistenceConcurrencyIT.FixedClockConfiguration.class)
class PersistenceConcurrencyIT {

    private static final String ITEM_ID = "item-1";
    private static final Instant NOW = Instant.parse("2026-08-26T09:15:00.123456Z");

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:db/schema.sql");
    }

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private CreateOrderUseCase createOrder;
    @Autowired
    private TransitionOrderUseCase transitionOrder;
    @Autowired
    private WebApplicationContext webContext;
    @MockitoSpyBean
    private OrderRepositoryPort orders;
    @MockitoSpyBean
    private ItemRepositoryPort items;

    @BeforeEach
    void resetDatabase() {
        jdbc.execute("drop trigger if exists reject_history on order_history");
        jdbc.execute("truncate orders, order_lines, order_history, items restart identity");
        insertItem(ITEM_ID);
    }

    @Test
    void givenLimitedStockWhenTwoOrdersReserveConcurrentlyThenOnlyOneIsCreated() throws Exception {
        CountDownLatch attempts = new CountDownLatch(2);
        doAnswer(invocation -> {
            arriveAndWait(attempts);
            return invocation.callRealMethod();
        }).when(items).findAllByIdsForUpdate(List.of(ITEM_ID));

        assertThat(concurrently(() -> tryReserve(4), () -> tryReserve(4)))
                .containsExactlyInAnyOrder(true, false);
        assertThat(count("orders")).isEqualTo(1);
        assertThat(count("order_history")).isEqualTo(1);
        assertStock(5, 4);
    }

    @Test
    void givenDeferredHistoryFailureWhenCreatingOrderThenOrderAndReservationRollBack() {
        rejectHistoryAtCommit(5, 2);

        assertThatThrownBy(() -> createOrder.execute(command(2))).hasStackTraceContaining("history rejected by test");

        assertThat(count("orders")).isZero();
        assertThat(count("order_lines")).isZero();
        assertThat(count("order_history")).isZero();
        assertStock(5, 0);
    }

    @Test
    void givenDeferredHistoryFailureWhenCancellingThenStateAndReleaseRollBack() {
        String orderId = createOrder.execute(command(2)).orderId();
        rejectHistoryAtCommit(5, 0);

        assertThatThrownBy(() -> transition(orderId, OrderEvent.CANCEL)).hasStackTraceContaining("history rejected by test");

        assertOrder(orderId, OrderStatus.MANAGED, 1, 0);
        assertStock(5, 2);
    }

    @Test
    void givenDeferredHistoryFailureWhenShippingThenStateAndDecrementRollBack() {
        String orderId = prepareOrder();
        rejectHistoryAtCommit(3, 0);

        assertThatThrownBy(() -> transition(orderId, OrderEvent.MARK_SHIPPED)).hasStackTraceContaining("history rejected by test");

        assertOrder(orderId, OrderStatus.IN_PREPARATION, 2, 1);
        assertStock(5, 2);
    }

    @Test
    void givenSameVersionWhenStartingPreparationTwiceThenCommitConflictIsIdempotent() throws Exception {
        String orderId = createOrder.execute(command(2)).orderId();
        synchronizeInitialReads(orderId);

        assertRepeatedTransition(orderId, OrderEvent.START_PREPARATION, OrderStatus.IN_PREPARATION);

        assertOrder(orderId, OrderStatus.IN_PREPARATION, 2, 1);
        assertStock(5, 2);
    }

    @Test
    void givenSameVersionWhenShippingTwiceThenOnlyOneRequestDecrementsStock() throws Exception {
        String orderId = prepareOrder();
        synchronizeInitialReads(orderId);

        assertRepeatedTransition(orderId, OrderEvent.MARK_SHIPPED, OrderStatus.SHIPPED);

        assertOrder(orderId, OrderStatus.SHIPPED, 3, 2);
        assertStock(3, 0);
    }

    @Test
    void givenSameVersionWhenTwoDifferentEventsCompeteThenTheUnsatisfiedEventConflicts() throws Exception {
        String orderId = createOrder.execute(command(2)).orderId();
        synchronizeInitialReads(orderId);

        List<Optional<OrderStatus>> outcomes = concurrently(
                () -> tryTransition(orderId, OrderEvent.START_PREPARATION),
                () -> tryTransition(orderId, OrderEvent.CANCEL));

        assertThat(outcomes).contains(Optional.empty());
        OrderStatus winner = outcomes.stream().flatMap(Optional::stream).findFirst().orElseThrow();
        assertOrder(orderId, winner, 2, 1);
        assertStock(5, winner == OrderStatus.CANCELLED ? 0 : 2);
    }

    @Test
    void givenTwoLinesWhenTransitioningThenExistingRowsKeepTheirIdentity() {
        insertItem("item-2");
        List<OrderLine> lines = List.of(new OrderLine("item-2", 1), new OrderLine(ITEM_ID, 2));
        String orderId = createOrder.execute(new CreateOrderCommand(List.of(
                new CreateOrderLineCommand("item-2", 1), new CreateOrderLineCommand(ITEM_ID, 2)))).orderId();
        List<Long> lineIds = jdbc.queryForList("select order_line_id from order_lines order by order_line_id", Long.class);
        Long initialHistoryId = jdbc.queryForObject("select order_history_id from order_history", Long.class);

        transition(orderId, OrderEvent.START_PREPARATION);

        assertThat(jdbc.queryForList("select order_line_id from order_lines order by order_line_id", Long.class))
                .containsExactlyElementsOf(lineIds);
        assertThat(jdbc.queryForList("select order_history_id from order_history order by order_history_id", Long.class))
                .hasSize(2).startsWith(initialHistoryId);
        Order order = orders.findById(orderId).orElseThrow();
        assertThat(order.lines()).containsExactlyElementsOf(lines);
        assertThat(order.history()).containsExactly(
                new OrderHistory(null, OrderStatus.MANAGED, NOW),
                new OrderHistory(OrderStatus.MANAGED, OrderStatus.IN_PREPARATION, NOW));
    }

    @Test
    void givenApplicationWiringWhenCallingAllFourRoutesThenWritesCommitAndReadsUseReadOnlyTransactions() throws Exception {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(webContext).build();
        String orderPath = mvc.perform(post("/orders").header("X-Trace-Id", "integration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lines":[{"itemId":"item-1","quantity":2}]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");
        assertThat(orderPath).startsWith("/orders/");

        mvc.perform(post(orderPath + "/transitions").header("X-Trace-Id", "integration")
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"event":"CANCEL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        doAnswer(invocation -> {
            assertReadOnlyTransaction();
            return invocation.callRealMethod();
        }).when(orders).findById(orderPath.substring("/orders/".length()));
        doAnswer(invocation -> {
            assertReadOnlyTransaction();
            return invocation.callRealMethod();
        }).when(items).findById(ITEM_ID);

        mvc.perform(get(orderPath).header("X-Trace-Id", "integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.history[1].newStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.lines[0].quantity").value(2));
        mvc.perform(get("/items/{itemId}/stock", ITEM_ID).header("X-Trace-Id", "integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockReserved").value(0))
                .andExpect(jsonPath("$.stockAvailable").value(5));
    }

    private static void assertReadOnlyTransaction() {
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isTrue();
    }

    private void synchronizeInitialReads(String orderId) {
        CountDownLatch reads = new CountDownLatch(2);
        AtomicInteger readCount = new AtomicInteger();
        doAnswer(invocation -> {
            Object order = invocation.callRealMethod();
            if (readCount.incrementAndGet() <= 2) {
                arriveAndWait(reads);
            }
            return order;
        }).when(orders).findById(orderId);
    }

    private void assertRepeatedTransition(String orderId, OrderEvent event, OrderStatus expectedStatus) throws Exception {
        List<TransitionOrderResult> results = concurrently(() -> transition(orderId, event), () -> transition(orderId, event));
        assertThat(results).extracting(TransitionOrderResult::idempotent).containsExactlyInAnyOrder(true, false);
        assertThat(results).extracting(TransitionOrderResult::status).containsExactly(expectedStatus, expectedStatus);
        assertThat(results).extracting(TransitionOrderResult::occurredAt).containsExactly(NOW, NOW);
    }

    private static <T> List<T> concurrently(Callable<T> first, Callable<T> second) throws Exception {
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<T> firstResult = executor.submit(first);
            Future<T> secondResult = executor.submit(second);
            try {
                return List.of(firstResult.get(20, TimeUnit.SECONDS), secondResult.get(20, TimeUnit.SECONDS));
            } finally {
                executor.shutdownNow();
            }
        }
    }

    private static void arriveAndWait(CountDownLatch latch) throws InterruptedException {
        latch.countDown();
        if (!latch.await(10, TimeUnit.SECONDS)) {
            throw new AssertionError("Concurrent operation did not reach the synchronization point");
        }
    }

    private void rejectHistoryAtCommit(int total, int reserved) {
        jdbc.execute("""
                create or replace function reject_history_write() returns trigger language plpgsql as $$
                begin
                    if not exists (select 1 from items where item_id = 'item-1'
                                   and stock_total = TG_ARGV[0]::integer
                                   and stock_reserved = TG_ARGV[1]::integer) then
                        raise exception 'stock was not written before commit';
                    end if;
                    raise exception 'history rejected by test' using errcode = '23514';
                end;
                $$
                """);
        jdbc.execute("""
                create constraint trigger reject_history after insert on order_history
                deferrable initially deferred for each row
                execute function reject_history_write('%d', '%d')
                """.formatted(total, reserved));
    }

    private void insertItem(String itemId) {
        jdbc.update("insert into items values (?, ?, ?, 5, 0)", itemId, "REF-" + itemId, "Test item");
    }

    private void assertStock(int total, int reserved) {
        assertThat(items.findById(ITEM_ID)).hasValueSatisfying(item -> {
            assertThat(item.stockTotal()).isEqualTo(total);
            assertThat(item.stockReserved()).isEqualTo(reserved);
            assertThat(item.stockAvailable()).isEqualTo(total - reserved);
        });
    }

    private void assertOrder(String orderId, OrderStatus status, int historySize, long version) {
        assertThat(orders.findById(orderId)).hasValueSatisfying(order -> {
            assertThat(order.status()).isEqualTo(status);
            assertThat(order.history()).hasSize(historySize);
            assertThat(order.version()).isEqualTo(version);
        });
    }

    private long count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Long.class);
    }

    private boolean tryReserve(int quantity) {
        try {
            createOrder.execute(command(quantity));
            return true;
        } catch (InsufficientStockException expected) {
            return false;
        }
    }

    private Optional<OrderStatus> tryTransition(String orderId, OrderEvent event) {
        try {
            return Optional.of(transition(orderId, event).status());
        } catch (ConcurrencyConflictException expected) {
            return Optional.empty();
        }
    }

    private String prepareOrder() {
        String orderId = createOrder.execute(command(2)).orderId();
        transition(orderId, OrderEvent.START_PREPARATION);
        return orderId;
    }

    private TransitionOrderResult transition(String orderId, OrderEvent event) {
        return transitionOrder.execute(new TransitionOrderCommand(orderId, event));
    }

    private static CreateOrderCommand command(int quantity) {
        return new CreateOrderCommand(List.of(new CreateOrderLineCommand(ITEM_ID, quantity)));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }
}
