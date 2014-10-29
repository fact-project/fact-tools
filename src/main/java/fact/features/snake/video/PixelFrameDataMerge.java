package fact.features.snake.video;

import java.io.Serializable;

import fact.Utils;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * PixelFrameDataMerge Fasst die unabhaengigen Stream Objekte aus einem loop
 * zusammen
 *
 * @author Dominik Baack &lt;dominik.baack@udo.edu&gt;
 *
 */
public class PixelFrameDataMerge implements Processor
{
	@Parameter(required = true, description = "Input: Name of Looped Data")
	private String key = null;

	@Parameter(required = true, description = "Output: Name of Items merged in array")
	private String[] listToMerge = null;

	@Override
	public Data process(Data input)
	{
		Utils.isKeyValid(input, key, Data[].class);

		Data[] items = (Data[]) input.get(key);

		for(String key : listToMerge)
		{

			if( items.length > 0 )
			{
				Object[] obj = null;
				if( items[0].get(key) instanceof Double )
				{
					obj = new Double[items.length];
					for(int i = 0; i < items.length; i++)
					{
						if( items[i].containsKey(key) )
						{
							Utils.isKeyValid(items[i], key, Double.class);
							obj[i] = items[i].get(key);
						}
						else
						{
							obj[i] = 0.0;
						}
					}
				}
				else if( items[0].get(key) instanceof Float )
				{
					obj = new Integer[items.length];
					for(int i = 0; i < items.length; i++)
					{
						if( items[i].containsKey(key) )
						{
							Utils.isKeyValid(items[i], key, Float.class);
							obj[i] = items[i].get(key);
						}
						else
						{
							obj[i] = 0.0f;
						}
					}
				}
				else if( items[0].get(key) instanceof Integer )
				{
					obj = new Integer[items.length];
					for(int i = 0; i < items.length; i++)
					{
						if( items[i].containsKey(key) )
						{
							Utils.isKeyValid(items[i], key, Integer.class);
							obj[i] = items[i].get(key);
						}
						else
						{
							obj[i] = 0;
						}
					}
				}
				else
				{
					obj = new Serializable[items.length];
					for(int i = 0; i < items.length; i++)
					{
						if( items[i].containsKey(key) )
						{
							obj[i] = items[i].get(key);
						}
						else
						{
							obj[i] = null;
						}
					}
				}

				input.put(key + "_merged", obj);
			}
			else
			{
				input.put(key + "_merged", null);
			}
		}

		input.remove(key);

		return input;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String[] getListToMerge()
	{
		return listToMerge;
	}

	public void setListToMerge(String[] listToMerge)
	{
		this.listToMerge = listToMerge;
	}

}
