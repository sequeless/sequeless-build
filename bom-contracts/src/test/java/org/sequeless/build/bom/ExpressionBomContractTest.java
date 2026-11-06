package org.sequeless.build.bom;

import org.sequeless.expression.api.ExpressionPort;
import org.sequeless.expression.jexl.JexlExpressionPort;
import org.sequeless.expression.testkit.ExpressionContract;

/**
 * Runs the expression testkit contract against the JEXL adapter at the versions {@code sequeless-bom}
 * lists. A failure here means a released expression artifact no longer satisfies its own contract
 * together with the rest of the BOM.
 */
class ExpressionBomContractTest extends ExpressionContract {

    @Override
    protected ExpressionPort port() {
        return new JexlExpressionPort();
    }
}
