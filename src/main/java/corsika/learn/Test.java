package corsika.learn;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;



public class Test
{

	public static void main(String[] args)
	{
		try
		{
			DataSource source = new DataSource("/home/dominik/Schreibtisch/iris.csv");
			
			Instances data = source.getDataSet();
			
			System.out.println( data.toSummaryString() );
			
			System.out.println("----------------------");
			
			
			
			data.setClassIndex(data.numAttributes() - 1);
			
			J48 tree = new J48();
			String[] options = new String[1];
			options[0] = "-U";
			
			tree.setOptions(options);
			tree.buildClassifier(data);
			
			System.out.println("-------------------------------");
			
			System.out.println( tree.toSummaryString() );
			
			
			double t = tree.classifyInstance( data.instance(3) );
			
			System.out.println(t);
			
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
