package org.sequeless.build.bom;

import java.util.Set;
import org.sequeless.ontology.api.OntologyDocument;
import org.sequeless.ontology.jena.JenaOntologyPort;
import org.sequeless.ontology.jena.ReasonerSetting;
import org.sequeless.ontology.testfixtures.OntologyContract;
import org.sequeless.ontology.testfixtures.OntologyPorts;

/**
 * Runs the ontology testfixtures contract against the Jena adapter at the versions {@code sequeless-bom}
 * lists. A failure here means a released ontology artifact no longer satisfies its own contract
 * together with the rest of the BOM.
 */
class OntologyBomContractTest extends OntologyContract {

    // OWL, as in JenaOntologyPortContractTest: the only setting that detects the inconsistency fixture.
    @Override
    protected OntologyPorts portFor(OntologyDocument document) {
        return OntologyPorts.of(JenaOntologyPort.fromDocument(document, ReasonerSetting.OWL, () -> Set.of("workload")));
    }
}
