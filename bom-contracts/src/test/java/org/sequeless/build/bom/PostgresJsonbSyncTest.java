package org.sequeless.build.bom;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sequeless.kernel.identity.Principal;
import org.sequeless.kernel.identity.Scope;
import org.sequeless.kernel.identity.TenantId;
import org.sequeless.kernel.values.ObjectId;
import org.sequeless.kernel.values.Value;
import org.sequeless.query.api.ObjectView;
import org.sequeless.query.api.ReadRequest;
import org.sequeless.query.postgres.PostgresReadPort;
import org.sequeless.spi.object.Audit;
import org.sequeless.spi.object.BusinessObject;
import org.sequeless.spi.object.ChangeSet;
import org.sequeless.spi.object.Create;
import org.sequeless.spi.object.PropertyRef;
import org.sequeless.spi.object.TypeRef;
import org.sequeless.store.postgres.PostgresObjectStore;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The O2 sync check (spec 18, section 2; #71). The store's {@code ValueJsonCodec} writes {@code sq_object.props}, and the
 * query adapter's {@code ValueJsonDecoder} reads it back. Both are package-private, so this test runs them through their
 * public ports in PostgreSQL 18: write with {@link PostgresObjectStore}, read by id with {@link PostgresReadPort}, and
 * compare every {@code Value} variant. {@code PostgresQueryStore} is not used, because it aggregates in SQL and never
 * decodes {@code props}.
 *
 * <p>The values are compared keyed by full property IRI, and that comparison is deliberate. The store's and the query's
 * {@code PropertyRef} are distinct records with the same single {@code iri} component, so comparing the raw maps fails
 * even when every value matches. The JSONB shape is keyed by IRI, so an IRI comparison is the wire contract this test
 * checks.
 */
class PostgresJsonbSyncTest {

    private static final String NS = "https://example.org/o2#";

    @Test
    void valuesWrittenByTheStoreReadBackThroughTheQueryPortUnchanged() {
        JdbcClient jdbcClient = JdbcClient.create(StorePostgres.dataSource());
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(StorePostgres.dataSource());
        Scope scope = new Scope(TenantId.DEFAULT, Principal.ANONYMOUS);
        ObjectId id = ObjectId.random();
        Instant at = Instant.parse("2026-01-01T00:00:00Z");
        Map<PropertyRef, Value> properties = Map.of(
                new PropertyRef(NS + "text"), Value.text("hello"),
                new PropertyRef(NS + "integer"), Value.integer(42L),
                new PropertyRef(NS + "decimal"), Value.decimal(new BigDecimal("12.50")),
                new PropertyRef(NS + "bool"), Value.bool(true),
                new PropertyRef(NS + "dateTime"), Value.dateTime(at),
                new PropertyRef(NS + "date"), Value.date(LocalDate.of(2026, 1, 1)),
                new PropertyRef(NS + "ref"), Value.ref(ObjectId.random()),
                new PropertyRef(NS + "list"), Value.list(List.of(Value.text("a"), Value.integer(1L))));
        BusinessObject object = new BusinessObject(
                id,
                new TypeRef(NS + "TypeA"),
                scope.tenantId(),
                1,
                Optional.empty(),
                properties,
                new Audit(at, "alice", at, "alice"),
                false);

        new PostgresObjectStore(jdbcClient, transactionManager)
                .commit(scope, new ChangeSet(List.of(new Create(object)), List.of()));
        Optional<ObjectView> read =
                new PostgresReadPort(jdbcClient, transactionManager).read(scope, new ReadRequest(id));

        assertThat(read).isPresent();
        // The two PropertyRef records (store and query) are distinct classes, so the maps are keyed by IRI here.
        Map<String, Value> writtenByIri = properties.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().iri(), Map.Entry::getValue));
        Map<String, Value> readByIri = read.orElseThrow().properties().entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().iri(), Map.Entry::getValue));
        assertThat(readByIri.keySet()).as("property IRIs read back").isEqualTo(writtenByIri.keySet());
        assertThat(readByIri).isEqualTo(writtenByIri);
    }

    @AfterEach
    void truncateTables() {
        StorePostgres.truncate();
    }
}
