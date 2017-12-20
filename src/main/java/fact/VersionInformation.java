package fact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Singleton class that provides version information about fact-tools.
 */
public class VersionInformation {

    private final Logger log = LoggerFactory.getLogger(VersionInformation.class);

    /**
     * Version from maven pom.xml
     */
    public final String version;

    /**
     * The git commit hash
     */
    public final String commitHash;

    /**
     * Last tag, including light-weight tags
     */
    public final String closestTag;

    /**
     * Number of commits since last tag
     */
    public final int commitsSinceClosestTag;

    /**
     * Output of git describe, $(LAST_TAG)-$(COMMITS_SINCE_LAST_TAG)-$(COMMIT_HASH)
     */
    public final String gitDescribe;

    private static VersionInformation instance;

    public static VersionInformation getInstance() {
        if (instance == null) {
            instance = new VersionInformation();
        }

        return instance;
    }


    private static String getVersionFromProperties() throws IOException {
        Properties p = new Properties();
        InputStream is = VersionInformation.class.getResourceAsStream("/project.properties");
        p.load(is);
        if (is == null) {
            throw new IOException("Could not load version from project.properties");
        }
        String version = p.getProperty("version");
        if (version == null) {
            throw new IOException("project.properties did not contain 'version'");
        }
        return version;
    }

    public static String getVersionFromPackage() throws RuntimeException {
        String version;
        Package facttoolsPackage = VersionInformation.class.getPackage();
        version = facttoolsPackage.getImplementationVersion();
        if (version == null) {
            version = facttoolsPackage.getSpecificationVersion();
        }

        if (version == null) {
            throw new RuntimeException("Could not determine version from package info");
        }

        return version;
    }

    private VersionInformation() {


        // get version info
        String version = "";
        // try to load from maven properties first
        try {
            version = getVersionFromProperties();
        } catch (IOException e) {
            log.error(e.getMessage());
            try {
                version = getVersionFromPackage();
            } catch (RuntimeException e2) {
                log.error(e2.getMessage());
            }
        }

        this.version = version;


        // get commitHash info
        Properties p = new Properties();
        InputStream is = run.class.getResourceAsStream("/git.properties");
        if (is != null) {
            try {
                p.load(is);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

        commitHash = p.getProperty("git.commit.id", "");
        closestTag = p.getProperty("git.closest.tag.name", "");
        commitsSinceClosestTag = Integer.parseInt(p.getProperty("git.closest.tag.commit.count", "-1"));
        gitDescribe = p.getProperty("git.commit.id.describe", "");

    }
}
