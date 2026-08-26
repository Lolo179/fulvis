package com.fulvis.openapi;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractTest {

    private final Map<String, Object> openApi = loadOpenApi();

    @Test
    void givenOpenApiContractThenItDefinesRequiredEndpoints() {
        Map<String, Object> paths = map(openApi.get("paths"));

        assertThat(paths.keySet()).containsExactlyInAnyOrder(
                "/orders",
                "/orders/{orderId}/transitions",
                "/orders/{orderId}",
                "/items/{itemId}/stock"
        );
    }

    @Test
    void givenOpenApiOperationsThenEveryOperationRequiresTraceIdHeader() {
        Map<String, Object> paths = map(openApi.get("paths"));

        for (Object pathValue : paths.values()) {
            Map<String, Object> path = map(pathValue);
            for (Object operationValue : path.values()) {
                Map<String, Object> operation = map(operationValue);
                List<Object> parameters = list(operation.get("parameters"));

                assertThat(parameters)
                        .anySatisfy(parameter -> assertThat(map(parameter).get("$ref"))
                                .isEqualTo("#/components/parameters/TraceIdHeader"));
            }
        }
    }

    @Test
    void givenTransitionRequestThenItAcceptsEventButNotTargetStatus() {
        Map<String, Object> schema = schema("TransitionOrderRequest");
        Map<String, Object> properties = map(schema.get("properties"));

        assertThat(properties.keySet()).containsExactly("event");
        assertThat(properties).doesNotContainKey("status");
        assertThat(list(schema.get("required"))).containsExactly("event");
    }

    @Test
    void givenOrderEventSchemaThenItDoesNotExposeOrderCreated() {
        List<Object> events = list(schema("OrderEvent").get("enum"));

        assertThat(events).containsExactly(
                "START_PREPARATION",
                "MARK_SHIPPED",
                "CONFIRM_RECEPTION",
                "CLOSE",
                "CANCEL"
        );
        assertThat(events).doesNotContain("ORDER_CREATED");
    }

    @Test
    void givenOrderStatusSchemaThenItContainsOnlyDocumentedStatuses() {
        assertThat(list(schema("OrderStatus").get("enum"))).containsExactly(
                "MANAGED",
                "IN_PREPARATION",
                "SHIPPED",
                "RECEIVED",
                "CLOSED",
                "CANCELLED"
        );
    }

    @Test
    void givenTransitionResponseThenItDoesNotExposeIdempotentFlag() {
        Map<String, Object> properties = map(schema("TransitionOrderResponse").get("properties"));

        assertThat(properties.keySet()).containsExactlyInAnyOrder("orderId", "status", "occurredAt");
        assertThat(properties).doesNotContainKey("idempotent");
    }

    @Test
    void givenErrorResponseThenItContainsStableContractFields() {
        Map<String, Object> errorResponse = schema("ErrorResponse");

        assertThat(list(errorResponse.get("required"))).containsExactly("error", "message", "traceId");
        assertThat(map(errorResponse.get("properties")).keySet()).containsExactlyInAnyOrder("error", "message", "traceId");
        assertThat(list(schema("ErrorCode").get("enum"))).contains(
                "OrderNotFound",
                "ItemNotFound",
                "InvalidOrderTransition",
                "InsufficientStock",
                "InvalidOrderLines",
                "ConcurrencyConflict",
                "MissingTraceId"
        );
    }

    private static Map<String, Object> loadOpenApi() {
        InputStream inputStream = OpenApiContractTest.class.getResourceAsStream("/openapi/fulvis-openapi.yaml");
        assertThat(inputStream).isNotNull();
        return new Yaml().load(inputStream);
    }

    private Map<String, Object> schema(String name) {
        return map(map(map(openApi.get("components")).get("schemas")).get(name));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object value) {
        return (List<Object>) value;
    }
}
