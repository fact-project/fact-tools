package fact.features;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

/**
 * Created by lena on 09.02.16.
 */
public class SetLabel implements Processor {

    @Parameter(required = true, description = "Label: Proton = 0, Gamma = 1")
    public int label;

    @Override
    public Data process(Data input) {

        input.put("label", label);

        return input;
    }
}
