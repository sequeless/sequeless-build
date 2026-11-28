package org.sequeless.build.bom;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.sequeless.kernel.identity.Scope;
import org.sequeless.ontology.schema.api.Schema;
import org.sequeless.ontology.spi.OntologyDocumentStore;
import org.sequeless.spi.object.BusinessObject;
import org.sequeless.store.spi.ObjectStorePort;
import org.sequeless.store.spi.ValidationPort;
import org.sequeless.store.spi.Violation;

/**
 * The G2 compile and link check for the released store artifacts at the versions this module pins (spec 18,
 * U10). It needs no database.
 *
 * <p>The check is compile-time: the ontology types that {@code store-core} puts in its public signatures
 * ({@link OntologyDocumentStore}, {@link Schema}) must resolve against {@code ontology-core} as the BOM lists it.
 * If the versions diverge, this class fails to compile.
 *
 * <p>The store-testkit contracts run in their own classes (spec 18, #71), against a PostgreSQL 18 container:
 * {@code PostgresObjectStoreContractTest} ({@code ObjectStoreContract}), {@code PostgresAggregateContractTest}
 * ({@code AggregateContract}), {@code AuthorizationContractOnBomTest} ({@code AuthorizationContract}, with a BOM-local
 * port), and {@code PostgresJsonbSyncTest} (the O2 JSONB sync check, through the public ports). The store's in-memory
 * doubles and JSONB codecs are package-private, so the database is the only public seam (#71, option 2).
 *
 * <p><b>Recorded gap, not coverage.</b> {@code ValidationContract} is not run in BOM CI yet. It needs a BOM-local
 * {@code ValidationPort} and a {@code Schema} built from an ontology document. It is deferred to sequeless-build#77,
 * and #71 and the spec 18 section 11 checkbox stay open until that follow-up lands.
 */
class StoreBomContractTest {

    private static final String POSTGRES_OBJECT_STORE = "org.sequeless.store.postgres.PostgresObjectStore";

    @Test
    void storeCoreSignaturesResolveAgainstOntologyCore() {
        Function<ObjectStorePort, OntologyDocumentStore> ontologyDocuments = ObjectStorePort::ontologyDocuments;
        ValidationPort validation = (Scope scope, Schema schema, BusinessObject object) -> List.<Violation>of();

        assertThat(ontologyDocuments).isNotNull();
        assertThat(validation).isNotNull();
    }

    @Test
    void postgresObjectStoreIsAStorePort() throws ClassNotFoundException {
        Class<?> adapter =
                Class.forName(POSTGRES_OBJECT_STORE, false, getClass().getClassLoader());

        assertThat(ObjectStorePort.class.isAssignableFrom(adapter)).isTrue();
    }
}
