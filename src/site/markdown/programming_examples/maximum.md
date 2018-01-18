#Find the maximum slice

This processor simply iterates over all slices in each pixel and stores the maximum amplitude of all pixels.
At first we need to get the actual telescope data from the input. The data is stored in one large 
1D array of constant length. 
Once the maximum is found we put it back into the input. Now heres the catch. Since you can only store things of 
type `Serializable` into the map we cannot use a primitive `double` variable but the corresponding `Double` 
object. 

        package fact.features;

        //imports ...

        /**
         * Documentation goes here
         */
        public class MaxAmp implements Processor {

          private String key = "DataCalibrated";
          private String outputKey = "maximum_amplitude;

          @Override
          public Data process(Data item) {
            // get the data from the input
            double[] data = (double[]) input.get(key);
            // here we save the amplitude
            Double maxAmplitude = 0;

            //get the roi. The Region of interest. Thats the length of an event in slices
            int roi = (Integer) input.get("NROI");
            
            
            // for each pixel
            for(int pix = 0 ; pix < Constants.NUMBEROFPIXELS; pix++){
              //get the position of the first slice of pixel pix in the 1D array.
              int position = pix*roi;
              //iterate over all slices
              for(int slice = position; slice < position + roi; slice ++){
                //and find the maximum
                if(data[slice] > maxAmplitude){
                  maxAmplitude = data[slice]
                }
              }
            }

            //now we save the amplitude under the name outputKey in the map and return the input
            input.put(outputKey, maxAmplitude)
          }
        }


We use two loops here to exemplify how to iterate over each pixel separately
event though this could easily be accomplished in a single loop. Another way to make this a bit quicker
would be to store the maximum amplitude in a primitive double and then cast it to a `Double` before storing
it in the map. 
You can read [this](http://stackoverflow.com/questions/5199359/why-do-people-still-use-primitive-types-in-java) 
discussion on the primitive types vs Number Objects in the Java programming language. 

If you don't worry about runtime and memory consumption you can also iterate over the pixel data by using
the methods provided by the FactPixelMap singelton.
            
            int roi = (Integer) input.get("NROI");
            for (FactCameraPixel p : map.pixelArray){
                double[] pixelData = p.getPixelData(data, roi);
            }
 
This creates a copy of the data for the specified pixel. 
