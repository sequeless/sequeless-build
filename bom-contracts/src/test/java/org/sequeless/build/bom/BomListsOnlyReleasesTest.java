package org.sequeless.build.bom;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** The BOM must list released versions only (issue #23: main never references a SNAPSHOT). */
class BomListsOnlyReleasesTest {

    @Test
    void bomListsOnlyReleasedVersions() throws Exception {
        assertThat(snapshotEntries(Path.of("../sequeless-bom/pom.xml"))).isEmpty();
    }

    @Test
    void rejectsSnapshotEntry(@TempDir Path dir) throws Exception {
        Path pom = dir.resolve("pom.xml");
        Files.writeString(pom, """
                <project>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.sequeless</groupId>
                        <artifactId>sequeless-kernel</artifactId>
                        <version>0.1.0-SNAPSHOT</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        assertThat(snapshotEntries(pom)).containsExactly("org.sequeless:sequeless-kernel:0.1.0-SNAPSHOT");
    }

    @Test
    void rejectsSnapshotReachedThroughAProperty(@TempDir Path dir) throws Exception {
        Path pom = dir.resolve("pom.xml");
        Files.writeString(pom, """
                <project>
                  <properties>
                    <filter.version>0.1.1-SNAPSHOT</filter.version>
                  </properties>
                  <dependencyManagement>
                    <dependencies>
                      <dependency>
                        <groupId>org.sequeless</groupId>
                        <artifactId>sequeless-filter-core</artifactId>
                        <version>${filter.version}</version>
                      </dependency>
                    </dependencies>
                  </dependencyManagement>
                </project>
                """);

        assertThat(snapshotEntries(pom)).containsExactly("org.sequeless:sequeless-filter-core:0.1.1-SNAPSHOT");
    }

    /** Returns the org.sequeless managed dependencies whose version (direct or via a property) is a SNAPSHOT. */
    static List<String> snapshotEntries(Path pom) throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        var doc = factory.newDocumentBuilder().parse(pom.toFile());
        var root = doc.getDocumentElement();
        var properties = childElements(root, "properties");
        var snapshots = new ArrayList<String>();
        for (Element managed : childElements(root, "dependencyManagement")) {
            for (Element deps : childElements(managed, "dependencies")) {
                for (Element dep : childElements(deps, "dependency")) {
                    String group = text(dep, "groupId");
                    if (!"org.sequeless".equals(group)) {
                        continue;
                    }
                    String version = text(dep, "version");
                    if (version.startsWith("${") && version.endsWith("}")) {
                        String name = version.substring(2, version.length() - 1);
                        version = properties.stream()
                                .map(p -> text(p, name))
                                .filter(v -> !v.isEmpty())
                                .findFirst()
                                .orElse(version);
                    }
                    if (version.endsWith("-SNAPSHOT")) {
                        snapshots.add(group + ":" + text(dep, "artifactId") + ":" + version);
                    }
                }
            }
        }
        return snapshots;
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
