package org.sequeless.build.bom;

import org.junit.jupiter.api.AfterEach;
import org.sequeless.store.postgres.PostgresObjectStore;
import org.sequeless.store.postgres.PostgresQueryStore;
import org.sequeless.testkit.query.AggregateContract;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Runs {@link AggregateContract} against the released {@link PostgresQueryStore} in PostgreSQL 18, seeded through the
 * released {@link PostgresObjectStore} into the same tables (spec 18, #71).
 */
class PostgresAggregateContractTest extends AggregateContract {

    @Override
    protected Environment freshEnvironment() {
        JdbcClient jdbcClient = JdbcClient.create(StorePostgres.dataSource());
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(StorePostgres.dataSource());
        return new Environment(
                new PostgresObjectStore(jdbcClient, transactionManager),
                new PostgresQueryStore(jdbcClient, transactionManager));
    }

    @AfterEach
    void truncateTables() {
        StorePostgres.truncate();
    }
}
