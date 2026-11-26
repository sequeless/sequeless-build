package org.sequeless.build.bom;

import org.sequeless.knowledge.spi.InferencePort;
import org.sequeless.knowledge.testkit.FakeInferencePort;
import org.sequeless.knowledge.testkit.InferencePortContract;

/**
 * Runs the knowledge testkit's {@code InferencePort} contract against the recording fake at the versions
 * {@code sequeless-bom} lists. A failure here means a released knowledge artifact no longer satisfies its own
 * contract together with the rest of the BOM.
 */
class KnowledgeBomContractTest extends InferencePortContract {

    @Override
    protected InferencePort newPort() {
        return FakeInferencePort.replying("reply");
    }
}
