package fact;

import java.security.Permission;

public class RunFACTTools {

    static class SystemExitException extends SecurityException {
        final public int status;

        SystemExitException(int status) {
            super("SystemExit called with status code " + status);
            this.status = status;
        }
    }

    static class CatchSystemExitSecurityManager extends SecurityManager {
        @Override public void checkExit(int status) {
            throw new SystemExitException(status);
        }

        @Override public void checkPermission(Permission perm) {}
    }

    public static void runFACTTools(String[] args) throws Exception {
        System.clearProperty("drsfile");
        System.clearProperty("infile");
        System.clearProperty("outfile");
        System.clearProperty("auxFolder");
        System.clearProperty("pixelDelayFile");
        System.clearProperty("integralGainFile");
        System.clearProperty("aux_source");
        System.clearProperty("output");

        // Catch the system exit stream calls when an error happens
        SecurityManager before = System.getSecurityManager();
        System.setSecurityManager(new CatchSystemExitSecurityManager());
        try {
            fact.run.main(args);
        } finally {
            System.setSecurityManager(before);
        }
    }
}
