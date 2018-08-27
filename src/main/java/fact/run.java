package fact;


import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static java.lang.System.out;

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
            // ignore error that happens in multi threaded environments where handler has already been set
        }
        stream.run.main(args);
    }

    private static void handleArguments(String[] args) {
        if (args.length == 0) {
            out.println("fact-tools, version " + VersionInformation.getInstance().gitDescribe);
            out.println();
            out.println("No container file specified.");
            out.println();
            out.println("Usage: ");
            out.println("\tjava -jar <fact-tools-jar> /path/container-file.xml");
            out.println();
            System.exit(0);
        }

        for (String arg : args) {
            if (arg.equals("-v") || arg.equals("-version") || arg.equals("--version")) {
                out.println("project version: " + VersionInformation.getInstance().version);
                out.println("git description: " + VersionInformation.getInstance().gitDescribe);
                out.println("git commit hash: " + VersionInformation.getInstance().commitHash);
                System.exit(0);
            }
        }
    }

    public static class ClasspathURLStreamHandlerFactory implements URLStreamHandlerFactory {

        @Override
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if ("classpath".equals(protocol)) {
                return new ClasspathURLStreamHandler();
            }
            return null;
        }

        class ClasspathURLStreamHandler extends URLStreamHandler {
            /** The classloader to find resources from. */
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                final URL resourceUrl =   ClasspathURLStreamHandler.class.getResource(u.getPath());
                return resourceUrl.openConnection();
            }
        }
    }
}
