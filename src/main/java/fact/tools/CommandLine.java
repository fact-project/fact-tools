/**
 *
 */
package fact.tools;

import org.jfree.util.Log;
import stream.runtime.ProcessContainer;

import java.io.File;
import java.io.IOException;

/**
 * @author Christian Bockermann &lt;christian.bockermann@udo.edu&gt;
 */
public class CommandLine {

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println();
            System.out.println("Usage:");
            System.out
                    .println("\tjava -jar fact-tools.jar path/to/experiment.xml");
            System.out.println();
            System.exit(-1);
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.err.println("Cannot find file " + file.getAbsolutePath()
                    + " !");
            System.exit(-1);
        } else {
            try {
                ProcessContainer container = new ProcessContainer(file.toURI().toURL());
                container.run();
            } catch (IOException e) {
                Log.error("Could not open file");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                System.exit(-1);

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    public static void waitForReturn() {
        try {
            System.out.println("Press RETURN to continue...");
            System.in.read();
        } catch (Exception e) {
        }
    }
}
