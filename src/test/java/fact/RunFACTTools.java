package fact;

public class RunFACTTools {
    public static void runFACTTools(String[] args) throws Exception {
        System.clearProperty("drsfile");
        System.clearProperty("infile");
        System.clearProperty("outfile");
        System.clearProperty("auxFolder");
        System.clearProperty("pixelDelayFile");
        System.clearProperty("integralGainFile");
        System.clearProperty("aux_source");
        System.clearProperty("output");
        fact.run.main(args);
    }
}
