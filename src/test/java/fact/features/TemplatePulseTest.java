package fact.features;

import fact.Utils;
import junit.framework.Assert;
import org.junit.Test;
import fact.features.singlePulse.timeSeriesExtraction.TemplatePulse;

public class TemplatePulseTest {

    @Test
    public void testTimeSeries() {
        final double[] time = TemplatePulse.timeSeries(1024);

        Assert.assertTrue(time.length == 1024);

        for(int i=1; i<time.length; i++)
            Assert.assertTrue(time[i-1] < time[i]);
    }

    @Test
    public void testPulsFromJensBussDiplomaThesisMinMaxAmplitudes() {
        final double[] pulse = TemplatePulse.factSinglePePulse(1024);
        Assert.assertTrue(pulse.length == 1024);

        for(int i=0; i<pulse.length; i++) {
            Assert.assertTrue(pulse[i] >= 0.0);
            Assert.assertTrue(pulse[i] <= 1.0);
        }
    }

    @Test
    public void testPulsFromPerformancePaperMinMaxAmplitudes() {
        final double[] pulse = TemplatePulse.performancePaper(1024);
        Assert.assertTrue(pulse.length == 1024);

        for(int i=0; i<pulse.length; i++) {
            Assert.assertTrue(pulse[i] >= 0.0);
            Assert.assertTrue(pulse[i] <= 1.0);
        }
    }
}
