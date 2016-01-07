package fact.pixelsets;

import com.google.common.collect.Sets;
import fact.Utils;
import fact.hexmap.CameraPixel;
import fact.container.PixelSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Set;


/**
 * This processor gets two pixel sets (U and A) and returns the difference of these sets,
 * denoted U \ A, is the set of all members of U that are not members of A
 * Created by jebuss on 17.12.15.
 */
public class Difference implements Processor{
    static Logger log = LoggerFactory.getLogger(Difference.class);

    @Parameter(required = true, description = "key to the first set to be compared")
    private String setUKey;

    @Parameter(required = true, description = "key to the second set to be united")
    private String setAKey;

    @Parameter(required = true, description = "key to the output set which contains the difference")
    private String outsetKey;

    @Override
    public Data process(Data input) {

        if (!input.containsKey(setUKey)) {
            return input;
        }

        Utils.isKeyValid(input, setUKey, PixelSet.class);

        PixelSet setU = (PixelSet) input.get(setUKey);
        PixelSet setA;

        //check if inset2 is given, otherwise create an empty set
        if (input.containsKey(setAKey)) {
            Utils.isKeyValid(input, setAKey, PixelSet.class);
            setA = (PixelSet) input.get(setAKey);
        } else {
            //create an empty set if no set is handed over
            setA = new PixelSet();
        }

        try{
            Sets.SetView<CameraPixel> difference = Sets.difference(setU.set, setA.set);
            Set<CameraPixel> cameraPixels = difference.immutableCopy();
            PixelSet outset = new PixelSet(cameraPixels);
            input.put(outsetKey, outset);
        } catch (NullPointerException e){
            e.printStackTrace();
        }



        return input;
    }

    public void setSetUKey(String setUKey) {
        this.setUKey = setUKey;
    }

    public void setSetAKey(String setAKey) {
        this.setAKey = setAKey;
    }

    public void setOutsetKey(String outsetKey) {
        this.outsetKey = outsetKey;
    }
}
