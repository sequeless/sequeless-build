package org.sequeless.build.bom;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.sequeless.ontology.api.ValidationSchema;
import org.sequeless.ontology.schema.api.AttributeDefinition;
import org.sequeless.ontology.schema.api.Datatype;
import org.sequeless.ontology.schema.api.Schema;
import org.sequeless.ontology.schema.api.TypeDefinition;

/**
 * Presents a {@link Schema} as the {@link ValidationSchema} that the released SHACL port reads: which types exist, how they
 * relate by supertype, and the XSD datatype of each attribute.
 *
 * <p>The released {@code ontology-jena-adapter} 0.1.2 has the same rules in {@code SchemaValidationSchema}, but that
 * class is package-private, so this copy reproduces its three methods over the public schema-contract API (spec 18,
 * sequeless-build#77). The rules are those of sequeless-ontology {@code c9726a7}, as copied into the prototype's
 * {@code ValidationSchemaAdapter}. Delete this class when sequeless-ontology publishes a public bridge. It holds no state
 * beyond the schema it wraps.
 */
final class SchemaAsValidationSchema implements ValidationSchema {

    private final Schema schema;

    /**
     * @param schema the type system to expose; must not be {@code null}
     * @throws NullPointerException if {@code schema} is {@code null}
     */
    SchemaAsValidationSchema(Schema schema) {
        this.schema = Objects.requireNonNull(schema, "schema must not be null");
    }

    @Override
    public boolean hasType(String typeIri) {
        return schema.type(typeIri).isPresent();
    }

    /**
     * Walks the supertypes transitively, breadth first, starting from {@code typeIri}. A supertype the schema does not
     * declare stays in the closure but is not expanded. A repeated supertype is not added again, so a cycle terminates.
     *
     * @param typeIri a declared type IRI
     * @return {@code typeIri} and every supertype reachable from it, as an unmodifiable set that starts with {@code typeIri}
     */
    @Override
    public Set<String> typeAndSupertypes(String typeIri) {
        Set<String> closure = new LinkedHashSet<>();
        closure.add(typeIri);
        Deque<String> pending = new ArrayDeque<>(List.of(typeIri));
        while (!pending.isEmpty()) {
            Optional<TypeDefinition> current = schema.type(pending.poll());
            if (current.isEmpty()) {
                continue;
            }
            for (String superType : current.get().superTypes()) {
                if (closure.add(superType)) {
                    pending.add(superType);
                }
            }
        }
        return Collections.unmodifiableSet(closure);
    }

    /**
     * The XSD datatype IRI of the attribute that {@code typeIri} declares for {@code propertyIri}. When the type declares
     * the property more than once, the last declaration wins. The XSD string IRI is returned when the property is not an
     * attribute declared on the type, including when the type is not declared.
     *
     * @param typeIri a declared type IRI
     * @param propertyIri the property IRI
     * @return the XSD datatype IRI of the attribute, or the XSD string IRI
     */
    @Override
    public String xsdDatatypeOf(String typeIri, String propertyIri) {
        return schema.type(typeIri)
                .flatMap(type -> type.properties().stream()
                        .filter(property -> property.iri().equals(propertyIri))
                        .reduce((first, second) -> second))
                .filter(AttributeDefinition.class::isInstance)
                .map(AttributeDefinition.class::cast)
                .map(attribute -> attribute.datatype().xsdIri())
                .orElse(Datatype.STRING.xsdIri());
    }
}
