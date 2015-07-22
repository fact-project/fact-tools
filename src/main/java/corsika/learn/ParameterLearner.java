package corsika.learn;

import stream.Data;
import stream.ProcessContext;
import stream.Processor;
import stream.StatefulProcessor;
import weka.core.Instance;
import weka.core.Instances;

public class ParameterLearner implements StatefulProcessor
{
	private Instances data;
	
	
	
	@Override
	public Data process(Data input)
	{
		
		/*for( String i: input.keySet())
		{
			System.out.println(i);
		}*/
		
		
		double a = (float) input.get("MCorsikaEvtHeader.fTotalEnergy");
		double b = (float) input.get("MCorsikaEvtHeader.fMomentumX");
		double c = (float) input.get("MCorsikaEvtHeader.fMomentumY");
		double d = (float) input.get("MCorsikaEvtHeader.fMomentumZ");
		double e = (float) input.get("MMcEvt.fZFirstInteraction");

		Instance inst = new Instance(5);	
		
		return input;
	}

	@Override
	public void init(ProcessContext context) throws Exception
	{
		//data = new Instances(null,0);

	}

	@Override
	public void resetState() throws Exception
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() throws Exception
	{
		// TODO Auto-generated method stub

	}

}
