Example Processes
=================

This page shows some example processes defined in XML that explain how FACT data
can be processed with the *fact-tools*. Any of these XML snippets can be stored
into a file and can be executed by using the FactViewer jar file.

The FactViewer jar file provides a small `stream.run` method that needs to be
provided with the XML file to execute:

    # java -jar FactTools.jar experiment.xml

As soon as all items of the streams defined in the `experiment.xml` file have
been processed, the Java process terminates.



### Processing FACT Data

The following snippet shows a simple experiment that reads FACT events from a
FITS data file and applies the DRS calibration:

     <container>

        <Stream id="fact-data" class="fact.io.FitsDataStream"
                url="file:///tmp/fact-data.fits.gz" />


        <Process input="fact-data">
          
            <fact.io.DrsCalibration file="file:///tmp/fact.drs.fits.gz" />
        
            <fact.data.MaxAmplitude />
            
        </Process>

     </container>

The `DrsCalibration` class implements the DRS chip calibration based on data
obtained from previous pedestal runs. This data is read from `fact.drs.fits.gz`.
Afer this calibration step, the keys `Data` and `DataCalibrated` are available
in the data item.

The `MaxAmplitude` processor is by default applied to the keys `Data` and
`DataCalibrated`. It computes a float array of 1440 float values over the
time slices of each pixel and stores this array as `amplitude(Data)` and
`amplitude(DataCalibrated)`.


### Exporting Calibrated Data

Another simple example can be given by a process that reads FACT data from
a FITS file, applies the DRS calibration and writes out the `DataCalibrated`
part, i.e. the calibrated data, into a file in binary form.

     <container>

        <stream id="fact-data" class="fact.io.FactDataStream"
                url="file:///tmp/fact-data.fits.gz" />


        <process input="fact-data">
          
            <fact.io.DrsCalibration file="file:///tmp/fact.drs.fits.gz" />

            <fact.io.BinaryFactWriter key="DataCalibrated"
                                      file="calibrated-events.dat" />
            
        </process>

     </container>



### Simple Plots

A very simple processors is provided by `ExtractPixel`, which can be used
to extract the slices of a single pixel into a float array that will be separately
stored in the data item:


      ...
      
      <stream id="fact-data" ... />
      
      <process input="fact-data">
         
         <fact.io.ExtractPixel chid="430" name="pixel_430"/>

      </process> 
      ...

Using the plot processors, we can now plot this pixel against other properties contained
in the event data items, e.g. the event number stored in `eventNum`.
      

      <process input="fact-data">
         
         <fact.io.ExtractPixel chid="430" name="pixel_430" />
         
         <stream.plotter.Plotter x-axis="eventNum" y-axis="pixel_430" output="pixel_430.plot.dat" />
      
      </process>


Event Data Items
----------------

As outlined above, each FACT event basically consists of an 1440 by 300
matrix of float values stored as key `Data`. This basically represents a
sequence of float array of length 300 for each pixel. The order of this
sequence is in *continuous hardware ID*, called *chid*.

Most of the processors provided by the fact-tools library do use this
1440x300 matrix as input for computing further results. 
