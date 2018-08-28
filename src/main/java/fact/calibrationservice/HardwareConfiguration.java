package fact.calibrationservice;


import fact.container.PixelSet;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class HardwareConfiguration implements Comparable<HardwareConfiguration> {

    public final ZonedDateTime startTime;

    public PixelSet badPixels = new PixelSet();
    public PixelSet notUsablePixels = new PixelSet();

    public HardwareConfiguration(ZonedDateTime startTime) {
        this.startTime = startTime.withZoneSameInstant(ZoneOffset.UTC);
    }

    @Override
    public int compareTo(HardwareConfiguration o) {
        return this.startTime.compareTo(o.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HardwareConfiguration hC = (HardwareConfiguration) o;

        if (badPixels != null ? !badPixels.equals(hC.badPixels) : hC.badPixels != null) return false;
        if (notUsablePixels != null ? !notUsablePixels.equals(hC.notUsablePixels) : hC.notUsablePixels != null)
            return false;
        if (!startTime.equals(hC.startTime)) return false;

        return true;
    }
}
