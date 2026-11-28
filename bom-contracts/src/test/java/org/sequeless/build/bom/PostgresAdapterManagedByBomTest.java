package org.sequeless.build.bom;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The BOM must manage ontology-postgres-adapter at a released version, so a consumer can declare the adapter
 * without a version (spec 18, F1). This reads the reactor's sequeless-bom pom, as {@link BomListsOnlyReleasesTest}
 * does, so it runs before the BOM release. The version-less resolution through the imported BOM is the check that
 * follows the import move to 0.5.0.
 */
class PostgresAdapterManagedByBomTest {

    private static final String ADAPTER = "ontology-postgres-adapter";

    @Test
    void bomManagesAdapterAtReleasedVersion() throws Exception {
        Optional<String> version = managedVersion(Path.of("../sequeless-bom/pom.xml"));

        assertThat(version).isPresent();
        assertThat(version.orElseThrow()).matches("\\d+\\.\\d+\\.\\d+");
    }

    @Test
    void reportsNoVersionWhenBomDoesNotManageAdapter(@TempDir Path dir) throws Exception {
        Path pom = dir.resolve("pom.xml");
        Files.writeString(pom, """
                <project>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.sequeless</groupId>
                        <artifactId>ontology-core</artifactId>
                        <version>0.1.0</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        assertThat(managedVersion(pom)).isEmpty();
    }

    /** The version the pom manages for org.sequeless:ontology-postgres-adapter, or empty when it does not manage it. */
    static Optional<String> managedVersion(Path pom) throws Exception {
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
        for (Element managed : childElements(doc.getDocumentElement(), "dependencyManagement")) {
            for (Element deps : childElements(managed, "dependencies")) {
                for (Element dep : childElements(deps, "dependency")) {
                    if ("org.sequeless".equals(text(dep, "groupId")) && ADAPTER.equals(text(dep, "artifactId"))) {
                        return Optional.of(text(dep, "version"));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static List<Element> childElements(Element parent, String name) {
        var result = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element e && name.equals(e.getTagName())) {
                result.add(e);
            }
        }
        return result;
    }

    private static String text(Element parent, String name) {
        return childElements(parent, name).stream()
                .findFirst()
                .map(e -> e.getTextContent().trim())
                .orElse("");
    }
}
