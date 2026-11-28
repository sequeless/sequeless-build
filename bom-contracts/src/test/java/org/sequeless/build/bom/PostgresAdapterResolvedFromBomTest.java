package org.sequeless.build.bom;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * The version-less ontology-postgres-adapter dependency of this module resolves through the imported sequeless-bom
 * (spec 18, F1). The adapter's pom.properties on the test classpath must report the version the BOM manages. Maven
 * fails the build first if the BOM does not manage the adapter, so this test pins the resolved version to the BOM.
 */
class PostgresAdapterResolvedFromBomTest {

    private static final String POM_PROPERTIES =
            "META-INF/maven/org.sequeless/ontology-postgres-adapter/pom.properties";

    @Test
    void resolvesAdapterAtTheVersionTheBomManages() throws Exception {
        String managed = PostgresAdapterManagedByBomTest.managedVersion(Path.of("../sequeless-bom/pom.xml"))
                .orElseThrow();

        assertThat(resolvedVersion()).isEqualTo(managed);
    }

    private static String resolvedVersion() throws IOException {
        var properties = new Properties();
        try (InputStream in =
                PostgresAdapterResolvedFromBomTest.class.getClassLoader().getResourceAsStream(POM_PROPERTIES)) {
            assertThat(in).as("%s on the test classpath", POM_PROPERTIES).isNotNull();
            properties.load(in);
        }
        return properties.getProperty("version");
    }
}
