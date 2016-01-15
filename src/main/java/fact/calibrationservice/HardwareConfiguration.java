package fact.calibrationservice;

import org.joda.time.DateTime;

public class HardwareConfiguration implements Comparable<HardwareConfiguration> {
	
	private DateTime startTime;
	
	private int[] badPixels;
	
	private int[] notUsablePixels;
	
	public HardwareConfiguration(DateTime startTime){
		this.startTime = new DateTime(startTime);
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

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(DateTime startTime) {
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
