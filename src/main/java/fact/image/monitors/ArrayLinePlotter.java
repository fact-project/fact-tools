package fact.image.monitors;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.Data;
import stream.data.DataFactory;
import stream.plotter.PlotPanel;
import stream.plotter.Plotter;
import stream.util.KeyFilter;

public class ArrayLinePlotter extends Plotter {
	final PlotPanel plotPanel = new PlotPanel();
	static Logger log = LoggerFactory.getLogger(Plotter.class);

	
	
	
	@Override
	public Data processMatchingData(Data data) {
		
//		processed++;
		if (getKeys() == null) {
			plotPanel.dataArrived(data);
		} else {
			Data stats = DataFactory.create();
			Set<String> selected = KeyFilter.select(data, getKeys());
			for (String key : selected) {
				if (data.containsKey(key)) {
					try{
						float[] v = (float[]) data.get(key);
						
						for (int i = 0; i < v.length ; ++i){
							stats.put(key + "_" + i, v[i]);
						}
					} catch(ClassCastException e){
						stats.put(key, data.get(key));
					}
				} else {
					log.warn("Did not find key " + key + "in the dataItem" );
//					stats.put(key, 0.0d);
				}
			}
			// if (this.updateInterval == null || updateInterval % processed ==
			// 0)
			plotPanel.dataArrived(stats);
		}
		return data;
	}
}
