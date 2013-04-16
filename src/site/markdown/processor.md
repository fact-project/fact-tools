Processing FACT Events
======================

The *fact-tools* module provides classes for reading FACT data and establishing
a processing chain for inspecting and manipulating events in a stream fashion.

A FACT event is a series of 300 time slices for each of the 1440 pixels. This
makes a total of 43200 float values. These values are stored in a 1440 by 300
matrix (float array) and are by default stored as key `Data`.




### Processing FACT Data

The following snippet shows a simple experiment that reads FACT events from a
FITS data file and applies the DRS calibration:

     <experiment>

        <Stream id="fact-data" class="fact.io.FitsDataStream"
                url="file:///tmp/fact-data.fits.gz" />


        <Process input="fact-data">
          
            <fact.io.DrsCalibration file="file:///tmp/fact.drs.fits.gz" />
        
            <fact.data.MaxAmplitude />
            
        </Process>

     </experiment>

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

     <experiment>

        <Stream id="fact-data" class="fact.io.FitsDataStream"
                url="file:///tmp/fact-data.fits.gz" />


        <Process input="fact-data">
          
            <fact.io.DrsCalibration file="file:///tmp/fact.drs.fits.gz" />

            <fact.io.BinaryFactWriter key="DataCalibrated"
                                      file="calibrated-events.dat" />
            
        </Process>

     </experiment>



Event Data Items
----------------

As outlined above, each FACT event basically consists of an 1440 by 300
matrix of float values stored as key `Data`. This basically represents a
sequence of float array of length 300 for each pixel. The order of this
sequence is in *continuous hardware ID*, called *chid*.

Most of the processors provided by the fact-tools library do use this
1440x300 matrix as input for computing further results. 
