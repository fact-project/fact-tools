#Reading and Writing Data
The essential thing your program usually does is reading and writing of data. In our case we often read raw data from the telescope and 
perform various steps to reduce all the data to simple parameters which may or may not be relevant for a semantic analysis of the things
your experiment recorded. 


### Writing complete Events to a File

A simple example can be given by a process that reads FACT data from
a FITS file, applies the DRS calibration and writes out the `DataCalibrated`
part, i.e. the calibrated data, into a file in binary form.

     <container>

        <stream id="fact-data" class="fact.io.FactEventStream"
                url="file:///tmp/fact-data.fits.gz" />


        <process input="fact-data">
          
            <fact.datacorrection.DrsCalibration url="file:///tmp/fact.drs.fits.gz"
                                          key="data" outputKey="DataCalibrated"/>

            <fact.io.BinaryFactWriter key="DataCalibrated"
                                      file="calibrated-events.dat" />
            
        </process>

     </container>


### Writing keys to a file

While this might not seem very useful heres another example. Here we write out a calculated feature for each event in the data stream. 
Lets say we use the `MaxAmplitude` processor to calculate the maximum amplitude for each pixel in each event.
You're probably interested in the mean value of `MaxAmplitude` for each event (i.e.  you want the average over all 1440 pixel in each event).
To calculate the mean from an array you can use the `ArrayMean` processor in the fact.statistics package. The following example for a simple
.xml file does just that and writes the result to a file called `output.csv`

     <container>

        <Stream id="fact-data" class="fact.io.FITSStream"
                url="file:///tmp/fact-data.fits.gz" />


        <Process input="fact-data">
          
            <fact.datacorrection.DrsCalibration url="file:///tmp/fact.drs.fits.gz"
                                          key="data" outputKey="DataCalibrated"/>
        
            <fact.features.MaxAmplitude key="DataCalibrated" outputKey="maxAmplitude"/>

            <fact.statistics.ArrayMean key="maxAmplitude" outputKey="maxAmplitudeMean" />
            
            <stream.io.CsvWriter keys="maxAmplitudeMean" url="file:///tmp/output.csv" />
        </Process>
     </container>

Heres the output in the output.csv file:

    E5b-Mac-mini-002:~ kaibrugge$ cat /tmp/output.csv
        maxAmplitudeMean
        17.614393739392597
        14.588448515241033
        22.169573771009336
        14.296095578926428
        21.037508170910385
        125.59263993996835
        16.84055010006866
        15.485890265286749
    ...

Since you probably want to write out something to connect these numbers to events, you could also write out the event number of each event like so:
    
    <-- No spaces between keys--/>    
    <stream.io.CsvWriter keys="EventNum,maxAmplitudeMean" url="file:///tmp/output.csv" />


The output in the file will look accordingly:

    E5b-Mac-mini-002:~ kaibrugge$ cat /tmp/output.csv
        EventNum,maxAmplitudeMean
        71,17.614393739392597
        74,14.588448515241033
        75,22.169573771009336
        76,14.296095578926428
        77,21.037508170910385
        78,125.59263993996835
        80,16.84055010006866
        81,15.485890265286749
      ...

Now let's play pretend and say you want to write an object of a custom class to your output file. All the stream.io.CsvWriter does is call the `toString()` method of the
java object its trying to print.  You're set as long as you override the `toString()` method yourself. 
Theres is one catch however. If you want to write out an array to the file the output will look a little something like this:

    E5b-Mac-mini-002:~ kaibrugge$ cat /tmp/output.csv
		positionArray
		[I@3acafb56
		[I@786c730
		[I@45d6a56e
		[I@207f5580
		[I@75982fc1
		[I@5a676437
		[I@6855a338
		[I@4e4ee70b
		[I@1e22ab57
      ...

Thats because the .csv format does not initially support data types other than simple numbers or strings. For output
of more complex data types you could use the JSONWriter.

    <stream.io.JSONWriter keys="EventNum,maxAmplitudeMean,some_data_structure" url="file:///tmp/output.json" />


	
