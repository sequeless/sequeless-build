package org.sequeless.build.bom;

import org.junit.jupiter.api.Nested;
import org.sequeless.query.testkit.BrowseContract;
import org.sequeless.query.testkit.InMemoryBrowsePort;
import org.sequeless.query.testkit.InMemoryIndexPreparationPort;
import org.sequeless.query.testkit.InMemoryObjectStore;
import org.sequeless.query.testkit.InMemoryReadPort;
import org.sequeless.query.testkit.IndexPreparationContract;
import org.sequeless.query.testkit.ReadContract;

/**
 * Runs the three query testkit contracts (browse, read and index preparation) against the in-memory
 * references at the versions {@code sequeless-bom} lists. A failure here means a released query artifact
 * no longer satisfies its own contract together with the rest of the BOM.
 *
 * <p>A Java class has one superclass, so each contract runs in its own {@link Nested} class. Each
 * {@code freshEnvironment()} builds a new store, so no test sees another test's objects.
 */
class QueryBomContractTest {

    /** The browse contract against {@link InMemoryBrowsePort} over an {@link InMemoryObjectStore}. */
    @Nested
    class Browse extends BrowseContract {

        @Override
        protected Environment freshEnvironment() {
            InMemoryObjectStore store = new InMemoryObjectStore();
            return new Environment(store, new InMemoryBrowsePort(store));
        }
    }

    /** The read contract against {@link InMemoryReadPort} over an {@link InMemoryObjectStore}. */
    @Nested
    class Read extends ReadContract {

        @Override
        protected Environment freshEnvironment() {
            InMemoryObjectStore store = new InMemoryObjectStore();
            return new Environment(store, new InMemoryReadPort(store));
        }
    }

    /** The index preparation contract against {@link InMemoryIndexPreparationPort}, which registers synchronously. */
    @Nested
    class IndexPreparation extends IndexPreparationContract {

        @Override
        protected Environment freshEnvironment() {
            InMemoryIndexPreparationPort port = new InMemoryIndexPreparationPort();
            return new Environment(port, port::awaitPrepared);
        }
    }
}
