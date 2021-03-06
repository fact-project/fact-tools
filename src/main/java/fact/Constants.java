package fact;

public class Constants {

    public static final int N_PIXELS = 1440;
    public static final int N_FTUS = 40;
    public static final int N_PATCHES_FTU = 4;
    public static final int N_PIXELS_PER_PATCH =9;
    public static final double FOCAL_LENGTH_MM = 4889.0;
    public static final double PIXEL_SIZE_MM = 9.5;
    public static final int N_PATCHES = N_PIXELS/N_PIXELS_PER_PATCH;
    public static final double FOV_DEG = 4.5;
    /** Source: Vogler, Patrick E., DOI:10.3929/ETHZ-A-010568419,
     *  URL: https://www.research-collection.ethz.ch/handle/20.500.11850/108302,
     *  2015, P.56
     */
    public static final double MILLIVOLT_PER_DAC = 610e-3; //source 1 (above)
}


