package fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionInformation {

    private final Logger log = LoggerFactory.getLogger(VersionInformation.class);

    public final String version;
    public final String commit;

    private static VersionInformation instance;

    public static VersionInformation getInstance() {
        if (instance == null) {
            instance = new VersionInformation();
        }

        return instance;
    }

    private VersionInformation () {

        // get version info
        String version = null;
        // try to load from maven properties first
        try {
            Properties p = new Properties();
            InputStream is = run.class.getResourceAsStream("/META-INF/maven/de.sfb876/fact-tools/pom.properties");
            if (is != null) {
                p.load(is);
                version = p.getProperty("version");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            log.warn("Could not obtain fact-tools version from Maven properties");

            Package facttoolsPackage = VersionInformation.class.getPackage();
            if (facttoolsPackage != null) {
                version = facttoolsPackage.getImplementationVersion();
                if (version == null) {
                    version = facttoolsPackage.getSpecificationVersion();
                }
            }

            if (version == null) {
                // we could not compute the version so use a blank
                version = "";
                log.warn("Could neither obtain streams version from Java API");
            }
        }

        // get commit info
        String commit = "";
        Properties p = new Properties();
        InputStream is = run.class.getResourceAsStream("/git.properties");

        try {
            if (is != null) {
                p.load(is);
                commit = p.getProperty("git.commit.id", "");
            }
        } catch (IOException e) {}

        this.commit = commit;
        this.version = version;
    }
}
