package org.sequeless.build.bom;

import javax.sql.DataSource;
import org.sequeless.store.postgres.PostgresMigrations;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The one PostgreSQL 18 instance the store contract tests in this package share (spec 18, #71). It starts on first use
 * and is migrated once, with the released store migrations: {@link PostgresMigrations#migrate(DataSource)} runs the
 * command set and then the query set, which this module gets from {@code query-postgres-jsonb-adapter}. Testcontainers
 * stops the container when the JVM exits.
 *
 * <p>Each test class truncates the command tables after every test with {@link #truncate()}, so a test starts from an
 * empty store.
 */
final class StorePostgres {

    private static final PostgreSQLContainer CONTAINER = new PostgreSQLContainer("postgres:18");

    private static DataSource dataSource;

    private StorePostgres() {}

    /**
     * @return the migrated data source for the shared container, starting and migrating the container on first call
     */
    static synchronized DataSource dataSource() {
        if (dataSource == null) {
            CONTAINER.start();
            dataSource = new SimpleDriverDataSource(
                    new org.postgresql.Driver(),
                    CONTAINER.getJdbcUrl(),
                    CONTAINER.getUsername(),
                    CONTAINER.getPassword());
            PostgresMigrations.migrate(dataSource);
        }
        return dataSource;
    }

    /** Empties the tables the object and aggregate contracts write, so the next test starts from nothing. */
    static void truncate() {
        JdbcClient.create(dataSource())
                .sql("TRUNCATE TABLE sq_object, sq_ontology_document, sq_outbox")
                .update();
    }
}
