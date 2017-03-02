package fact.calibrationservice;


import java.time.*;

public class HardwareConfiguration implements Comparable<HardwareConfiguration> {
	
	private OffsetDateTime startTime;
	
	private int[] badPixels;
	
	private int[] notUsablePixels;
	
	public HardwareConfiguration(OffsetDateTime startTime){
		this.startTime = OffsetDateTime.of(startTime.toLocalDate(),startTime.toLocalTime(), ZoneOffset.of("+00:00"));
	}

	@Override
	public int compareTo(HardwareConfiguration o) {
		return this.startTime.compareTo(o.getStartTime());
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HardwareConfiguration hC = (HardwareConfiguration) o;

        if (badPixels != null ? !badPixels.equals(hC.badPixels) : hC.badPixels != null) return false;
        if (notUsablePixels != null ? !notUsablePixels.equals(hC.notUsablePixels) : hC.notUsablePixels != null) return false;
        if (!startTime.equals(hC.startTime)) return false;

        return true;
    }

	public OffsetDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(OffsetDateTime startTime) {
		this.startTime = startTime;
	}

	public int[] getBadPixels() {
		return badPixels;
	}

	public void setBadPixels(int[] badPixels) {
		this.badPixels = badPixels;
	}

	public int[] getNotUsablePixels() {
		return notUsablePixels;
	}

	public void setNotUsablePixels(int[] notUsablePixels) {
		this.notUsablePixels = notUsablePixels;
	}
	
	

}
