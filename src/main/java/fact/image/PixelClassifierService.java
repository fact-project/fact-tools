/**
 * 
 */
package fact.image;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import stream.Data;
import stream.service.Service;

/**
 * @author chris
 * 
 */
public interface PixelClassifierService extends Service {

	public Map<Integer, Serializable> predict(List<Data> pixels);

	public void train(Collection<Data> pixels);
}
