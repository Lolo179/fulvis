package com.fulvis.adapters.out.persistence;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceSchemaTest {

    @Test
    void givenSchemaThenItPersistsStockTotalAndReservedButNotStockAvailable() throws IOException {
        String schema = loadSchema();

        assertThat(schema).contains("stock_total");
        assertThat(schema).contains("stock_reserved");
        assertThat(schema).doesNotContain("stock_available");
    }

    @Test
    void givenSchemaThenOrderHasVersionForOptimisticLocking() throws IOException {
        String schema = loadSchema();

        assertThat(schema).contains("version bigint not null");
    }

    private static String loadSchema() throws IOException {
        try (InputStream inputStream = PersistenceSchemaTest.class.getResourceAsStream("/db/schema.sql")) {
            assertThat(inputStream).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
