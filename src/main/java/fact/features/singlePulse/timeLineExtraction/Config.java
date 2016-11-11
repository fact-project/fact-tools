package fact.features.singlePulse.timeLineExtraction;


public class Config {
    public int pulseToLookForLength;
    public int offsetSlices;
    public int negativePulseLength;
    public double factSinglePeAmplitudeInMv;
    public int maxIterations;

    public Config() {
        pulseToLookForLength = 20;
        offsetSlices = 7;
        negativePulseLength = 300;
        factSinglePeAmplitudeInMv = 10.0;
        maxIterations = 250;
    }       
}