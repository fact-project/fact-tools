Programming Examples
=================

Even though most of the important processors for working with FACT data are already included, 
chances are you want to write your own. Working with the [streams-api](http://www.jwall.org/streams/stream-api/) 
allows easy implementation of new methods to analyze the data. For an example on how to write a processor
see [here](http://www.jwall.org/streams/stream-api/processor.html).
Below is a short example/template for a simple processor.

        package fact.features;

        //imports ...

        /**
         * Documentation goes here
         */
        public class MaxAmp implements Processor {

          @Override
          public Data process(Data item) {
            // get the data from the input
            double[] data = (double[]) input.get("DataCalibrated");
            //do something with the data
            ...
            //now we save it back to the data item
            input.put("name_for_modified_data", modifiedData);
          }
        }
