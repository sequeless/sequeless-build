package org.sequeless.build.bom;

import java.util.Objects;
import org.sequeless.kernel.identity.Scope;
import org.sequeless.store.api.AccessDecision;
import org.sequeless.store.api.Operation;
import org.sequeless.store.spi.AuthorizationPort;
import org.sequeless.testkit.authz.AuthorizationContract;

/**
 * Runs {@link AuthorizationContract} against a BOM-local port, because the store artifacts ship no public implementation
 * of {@link AuthorizationPort} (spec 18, #71). The contract constrains null-handling, never-throwing and determinism, not
 * the allow or deny outcome. The testkit's own deny-all test proves that a permit-all port passes it too.
 */
class AuthorizationContractOnBomTest extends AuthorizationContract {

    @Override
    protected AuthorizationPort port() {
        return new PermitAllAuthorizationPort();
    }

    /** Permits every request and checks its arguments, as the contract requires. */
    private static final class PermitAllAuthorizationPort implements AuthorizationPort {

        @Override
        public AccessDecision decide(Scope scope, Operation operation, String resource) {
            Objects.requireNonNull(scope, "scope must not be null");
            Objects.requireNonNull(operation, "operation must not be null");
            Objects.requireNonNull(resource, "resource must not be null");
            return AccessDecision.permit("bom contract permit-all port");
        }
    }
}
