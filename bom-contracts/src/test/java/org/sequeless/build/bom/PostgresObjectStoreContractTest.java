package org.sequeless.build.bom;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.sequeless.kernel.identity.Scope;
import org.sequeless.store.postgres.PostgresObjectStore;
import org.sequeless.store.spi.ObjectStorePort;
import org.sequeless.testkit.object.ObjectStoreContract;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Runs {@link ObjectStoreContract} against the released {@link PostgresObjectStore} in PostgreSQL 18, the same contract
 * the store's own {@code PostgresObjectStoreContractIT} runs, here against the BOM's versions (spec 18, #71).
 */
class PostgresObjectStoreContractTest extends ObjectStoreContract {

    @Override
    protected ObjectStorePort freshStore() {
        JdbcClient jdbcClient = JdbcClient.create(StorePostgres.dataSource());
        return new PostgresObjectStore(jdbcClient, new DataSourceTransactionManager(StorePostgres.dataSource()));
    }

    @Override
    protected List<UUID> persistedOutboxIds(Scope scope) {
        return JdbcClient.create(StorePostgres.dataSource())
                .sql("SELECT id FROM sq_outbox WHERE tenant_id = :tenantId")
                .param("tenantId", scope.tenantId().value())
                .query(UUID.class)
                .list();
    }

    @AfterEach
    void truncateTables() {
        StorePostgres.truncate();
    }
}
