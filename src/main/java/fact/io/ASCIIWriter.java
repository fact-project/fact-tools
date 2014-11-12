package fact.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import stream.expressions.version2.Expression;
import stream.expressions.version2.StringExpression;
import stream.io.CsvWriter;

public class ASCIIWriter implements StatefulProcessor
{
	// XML Key:

	@Parameter(required = false, description = "Input: Keys that will be ignored")
	private String[] inkeyIgnoredKeys = { "Data", "DataCalibrated" };

	@Parameter(required = false, description = "Input: Accept NaN and Inf or throw exception?")
	private boolean inkeyAcceptLimits = true;

	@Parameter(required = false, description = "Input: Accept null or throw exception?")
	private boolean inkeyAcceptNull = true;

	@Parameter(required = false, description = "Input: Accept Arrays (1), ignore Arrays (2) or throw exception (4)?")
	private int inkeyAcceptArrays = 2;

	@Parameter(required = true, description = "Input: Loglevel (![0-9]) ")
	private int logLevel = 2;

	@Parameter(required = true, description = "Input: Path to output file")
	private String outputFile = null;

	// Class Data:
	ProcessContext context = null;

	private Map<String, Boolean> ignoredKey = new HashMap<String, Boolean>();

	private Expression<String> fileExpression;
	private File file;
	private String lastPath = null;
	URL url = null;
	PrintStream p = null;

	private final int CONST_ASCII_0 = (int) '0';
	private final int CONST_ASCII_9 = (int) '9';

	// Function:

	private PrintStream setupFile(Data input) throws IOException
	{
		if( p != null )
			return p;

		String expandedUrlString = null;
		try
		{
			expandedUrlString = fileExpression.get(context, input);
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
		if( expandedUrlString == null )
		{
			throw new RuntimeException("Path not correct: " + outputFile);
		}
		if( lastPath == null || !expandedUrlString.equals(lastPath) )
			throw new RuntimeException("Change of file at runtime not implemented");

		lastPath = expandedUrlString;

		this.url = new URL(expandedUrlString);
		try
		{
			file = new File(url.toURI());
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException("URI Syntax exception");
		}

		OutputStream out;
		if( file.getAbsolutePath().endsWith(".gz") )
			out = new GZIPOutputStream(new FileOutputStream(file, false));
		else
			out = new FileOutputStream(file, false);

		p = new PrintStream(out, false, "UTF-8");

		return p;
	}

	public void write(PrintStream p, Serializable data, boolean lastElement)
	{
		
		
			
		
		if(lastElement)
		{			 	
			p.println();
			return;
		}
		p.print('\t');	// Seperator			
	}

	@Override
	public Data process(Data input)
	{
	//	try
		{
			//setupFile(input);
		}
		//catch (IOException e)
		{			
		//	e.printStackTrace();
		}
		
		

		//
		for(Entry<String, Serializable> itr : input.entrySet())
		{
			String key = itr.getKey();
			Serializable value = itr.getValue();

			// Key Filter
			if( key.charAt(0) == '@' || key.charAt(0) == '_' || ignoredKey.containsKey(key) )
			{
				continue;
			}

			// Content Filter
			if( value.getClass().isArray() )
			{
				if( inkeyAcceptArrays == 4 )
					throw new RuntimeException("Key " + key + " is Array, but no Arrays accepted!");
				else if( inkeyAcceptArrays == 2 )
					continue;
			}

			if( logLevel == 0 )
			{
				
			}
			else if( key.charAt(0) == '!' && (int) key.charAt(1) >= CONST_ASCII_0
					&& (int) key.charAt(1) <= CONST_ASCII_9 )
			{
				if( (int) key.charAt(0) <= (logLevel + CONST_ASCII_0) )
				{

				}
			}

			System.out.println(key);

		}

		return input;
	}

	@Override
	public void finish() throws Exception
	{

	}

	@Override
	public void init(ProcessContext ctx) throws Exception
	{
		context = ctx;
		fileExpression = new StringExpression(outputFile);

		//
		for(String itr : inkeyIgnoredKeys)
		{
			ignoredKey.put(itr, true);
		}

		//
		boolean flag = false;
		for(int i = 0; i < 10; i++)
		{
			if( logLevel == i )
				flag = true;
		}
		if( !flag )
			throw new RuntimeException("LogLevel is " + Integer.toString(logLevel) + " but must be between 0 and 9");

		//

	}

	@Override
	public void resetState() throws Exception
	{

	}

	// //////////////////////////
	// /Get and Set//////////////

	public String[] getInkeyIgnoredKeys()
	{
		return inkeyIgnoredKeys;
	}

	public void setInkeyIgnoredKeys(String[] inkeyIgnoredKeys)
	{
		this.inkeyIgnoredKeys = inkeyIgnoredKeys;
	}

	public boolean isInkeyAcceptLimits()
	{
		return inkeyAcceptLimits;
	}

	public void setInkeyAcceptLimits(boolean inkeyAcceptLimits)
	{
		this.inkeyAcceptLimits = inkeyAcceptLimits;
	}

	public boolean isInkeyAcceptNull()
	{
		return inkeyAcceptNull;
	}

	public void setInkeyAcceptNull(boolean inkeyAcceptNull)
	{
		this.inkeyAcceptNull = inkeyAcceptNull;
	}

	public int getInkeyAcceptArrays()
	{
		return inkeyAcceptArrays;
	}

	public void setInkeyAcceptArrays(int inkeyAcceptArrays)
	{
		this.inkeyAcceptArrays = inkeyAcceptArrays;
	}

	public int getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel(int logLevel)
	{
		this.logLevel = logLevel;
	}

	public String getOutputFile()
	{
		return outputFile;
	}

	public void setOutputFile(String outputFile)
	{
		this.outputFile = outputFile;
	}

}
