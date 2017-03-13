Example Processes
=================

This page shows some example processes defined in XML that explain how FACT data
can be processed with the *fact-tools*. Any of these XML snippets can be stored
into a file and can be executed by using the FactViewer jar file.

The FactViewer jar file provides a small `stream.run` method that needs to be
provided with the XML file to execute:

    # java -jar fact-tools-VERSION.jar experiment.xml

As soon as all items of the streams defined in the `experiment.xml` file have
been processed, the Java process terminates.



### Processing FACT Data

The following snippet shows a simple experiment that reads FACT events from a
FITS data file and applies the DRS calibration:

     <container>

        <Stream id="fact-data" class="fact.io.FITSStream"
                url="file:///tmp/fact-data.fits.gz" />


        <Process input="fact-data">
          
            <fact.datacorrection.DrsCalibration url="file:///tmp/fact.drs.fits.gz"
                                          key="data" outputKey="DataCalibrated"/>
        
            <fact.features.MaxAmplitude key="DataCalibrated" outputKey="maxAmplitude"/>
            
        </Process>

     </container>

The `DrsCalibration` class implements the DRS chip calibration based on data
obtained from previous pedestal runs. This data is read from `fact.drs.fits.gz`.
Afer this calibration step, the keys `Data` and `DataCalibrated` are available
in the data item.

The `MaxAmplitude` processor is applied to the key `DataCalibrated` as specified by the parameter in the `experiment.xml`.
It computes a double array of 1440 double values over the
time slices of each pixel and stores this array as `maxAmplitude`.




Event Data Items
----------------

As outlined above, each FACT event basically consists of an 1440 by 300
matrix of float values stored as key `Data`. This basically represents a
sequence of float array of length 300 for each pixel. The order of this
sequence is in *continuous hardware ID*, called *chid*.

Most of the processors provided by the fact-tools library do use this
1440x300 matrix as input for computing further results. 
