package org.sequeless.build.bom;

import java.util.Set;
import org.sequeless.kernel.identity.TenantId;
import org.sequeless.ontology.api.OntologyDocument;
import org.sequeless.ontology.jena.JenaOntologyPort;
import org.sequeless.ontology.jena.ReasonerSetting;
import org.sequeless.ontology.schema.api.Schema;
import org.sequeless.testkit.validation.ValidationContract;

/**
 * Runs the store-testkit {@link ValidationContract} against {@link BomValidationPort}, built on the released
 * {@code ontology-jena-adapter} 0.1.2 SHACL port (spec 18, sequeless-build#77). It needs no database.
 *
 * <p>The fixture follows the prototype's {@code ShaclValidationPortContractTest}: the {@link Schema} comes from the same
 * {@link JenaOntologyPort} that the SHACL port reads its shapes from. The reasoner is {@link ReasonerSetting#OWL}, the
 * prototype's choice, and it is held in one constant so it can change if the contract run requires it.
 */
class ValidationContractOnBomTest extends ValidationContract {

    private static final ReasonerSetting REASONER = ReasonerSetting.OWL;

    @Override
    protected Fixture fixtureFor(OntologyDocument document) {
        JenaOntologyPort ontologyPort = JenaOntologyPort.fromDocument(document, REASONER, () -> Set.of("workload"));
        Schema schema = ontologyPort.schema(TenantId.DEFAULT);
        return new Fixture(new BomValidationPort(ontologyPort), schema);
    }
}
