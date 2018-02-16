package fact;


import java.net.URL;

/**
 * Main executable for the FACT-Tools,
 * this is a thin wrapper around stream.run and only changes version and help text.
 */
public class run {
    public static void main(String[] args) throws Exception {
        handleArguments(args);

        // Support "classpath:/path/to/resource" URLs,
        try {
            URL.setURLStreamHandlerFactory(new ClasspathURLStreamHandlerFactory());
        } catch (Error e) {
            // ignore error that happens in multithreaded if handler already set
        }
        // start streams
        stream.run.main(args);
    }

    public static void handleArguments(String[] args) {
        if (args.length == 0) {
            System.out.println("fact-tools, version " + VersionInformation.getInstance().gitDescribe);
            System.out.println();
            System.out.println("No container file specified.");
            System.out.println();
            System.out.println("Usage: ");
            System.out.println("\tjava -jar <fact-tools-jar> /path/container-file.xml");
            System.out.println();
            System.exit(0);
        }

        for (String arg : args) {
            if (arg.equals("-v") || arg.equals("-version") || arg.equals("--version")) {
                System.out.println("project version: " + VersionInformation.getInstance().version);
                System.out.println("git description: " + VersionInformation.getInstance().gitDescribe);
                System.out.println("git commit hash: " + VersionInformation.getInstance().commitHash);
                System.exit(0);
            }
        }
    }
}
