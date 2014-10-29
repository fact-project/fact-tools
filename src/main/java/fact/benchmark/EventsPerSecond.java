package fact.benchmark;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class EventsPerSecond implements Processor
{
	private int eventCount = 0;
	private long cumuTime = 0;
	
	private long lastTiming = 0;
	
	
	private long average = 0;
	private int averageCounter = 0;
	
	@Parameter(required = false)
	private int averageOver = 1;
	
	private final double timerBase = Math.pow(10, 9);
	
	public EventsPerSecond() 
	{
		lastTiming = System.nanoTime();
	}
	
	@Override
	public Data process(Data input) 
	{
		long timeDif = System.nanoTime() - lastTiming;
		lastTiming = System.nanoTime();
		
		cumuTime += timeDif;
		eventCount++;
		
		average += timeDif;
		averageCounter++;
		
		if(averageCounter == averageOver)
		{
			System.out.println("Events per Second: " + ((timerBase/(double)average) * (double)averageOver) );
			
			averageCounter = 0;
			average = 0;
		}
		
		
		return input;
	}

	@Override
    protected void finalize() throws Throwable
	{
		System.out.println("Global Event average per Second: " + ((timerBase/(double)cumuTime) * (double)eventCount));
		super.finalize();
	}
	
	public int getAverageOver() {
		return averageOver;
	}

	public void setAverageOver(int averageOver) {
		this.averageOver = averageOver;
	}	
	
	
}


