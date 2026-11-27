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
 * <p><b>Recorded gap, not coverage.</b> This class does not run the store-testkit contracts
 * ({@code ObjectStoreContract}, {@code AggregateContract}, {@code ValidationContract},
 * {@code AuthorizationContract}), and it does not run the O2 JSONB-to-{@code Value} sync test. The store's in-memory
 * doubles are package-private, and the only public port implementations need a database. The O2 fallback is
 * recorded in sequeless-build#71. These contracts are added here once a public seam exists.
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
