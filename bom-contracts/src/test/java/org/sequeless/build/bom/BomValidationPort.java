package org.sequeless.build.bom;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.sequeless.kernel.identity.Scope;
import org.sequeless.kernel.values.Value;
import org.sequeless.ontology.api.ShapeViolation;
import org.sequeless.ontology.api.ValidatableObject;
import org.sequeless.ontology.jena.JenaOntologyPort;
import org.sequeless.ontology.jena.ShaclValidationPort;
import org.sequeless.ontology.schema.api.Schema;
import org.sequeless.spi.object.BusinessObject;
import org.sequeless.spi.object.PropertyRef;
import org.sequeless.store.spi.ValidationPort;
import org.sequeless.store.spi.Violation;

/**
 * A BOM-local {@link ValidationPort} (spec 18, sequeless-build#77). The store artifacts ship no public implementation, so this
 * class maps the store's {@link Schema} and {@link BusinessObject} to the released ontology types, delegates the SHACL
 * evaluation to the released {@link ShaclValidationPort} from {@code ontology-jena-adapter} 0.1.2, and maps each
 * {@link ShapeViolation} back to a {@link Violation}. It holds no SHACL logic of its own.
 *
 * <p>The released port reads its shapes from the live ontology of the {@link JenaOntologyPort} it is given, so pass the
 * same port that built the {@link Schema}.
 */
final class BomValidationPort implements ValidationPort {

    private final ShaclValidationPort delegate;

    /**
     * @param ontologyPort the released Jena adapter whose live ontology supplies the {@code sh:} shapes; must not be
     *     {@code null}
     * @throws NullPointerException if {@code ontologyPort} is {@code null}
     */
    BomValidationPort(JenaOntologyPort ontologyPort) {
        this.delegate = new ShaclValidationPort(Objects.requireNonNull(ontologyPort, "ontologyPort must not be null"));
    }

    /**
     * Checks every argument for {@code null} before anything else, so the {@link NullPointerException} comes from this class
     * and never from a side effect of the bridge or the released port.
     */
    @Override
    public List<Violation> validate(Scope scope, Schema schema, BusinessObject object) {
        Objects.requireNonNull(scope, "scope must not be null");
        Objects.requireNonNull(schema, "schema must not be null");
        Objects.requireNonNull(object, "object must not be null");

        List<ShapeViolation> shapeViolations =
                delegate.validate(scope, new SchemaAsValidationSchema(schema), toValidatable(object));
        return shapeViolations.stream()
                .map(violation -> new Violation(violation.propertyIri(), violation.message()))
                .toList();
    }

    /**
     * Keys each property by its IRI and keeps its value unchanged. {@link PropertyRef} equality is by IRI (it is a
     * record with the IRI as its only component), so the map holds one entry per IRI. A {@link BusinessObject} is keyed
     * only by the store-core {@code PropertyRef}, so the query-core {@code PropertyRef} never reaches this method. The
     * two-record collision recorded in sequeless-build#78 therefore does not arise here.
     *
     * @param object the object to convert; must not be {@code null}
     * @return the released form of {@code object}: its id, its type IRI, and its properties keyed by IRI
     */
    private static ValidatableObject toValidatable(BusinessObject object) {
        Map<String, Value> propertiesByIri = new LinkedHashMap<>();
        for (Map.Entry<PropertyRef, Value> property : object.properties().entrySet()) {
            propertiesByIri.put(property.getKey().iri(), property.getValue());
        }
        return new ValidatableObject(object.id(), object.type().iri(), propertiesByIri);
    }
}
