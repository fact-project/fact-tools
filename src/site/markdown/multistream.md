## Using Multistreams

You often want to analyze many files at once or sequentially without starting a new fact-tools instance for each
data .fits file. This is where multistreams come in handy.

You start a multistream by wrapping your input stream  (any kind of stream will do) in another multistream which handles 
the processing of multiple files. Heres an example .xml that can utilize multiple CPU cores by using the `copies` property.


As always the process needs be assigned to a specific data source. Since there are `num_copies` copies of the stream and
the process in this case, you need a unique `id` for each of these. You can access a specific copy by using the 
`${copy.id}` property.


### Using RecursiveDirectoryStreams

This particular MultiStream is quite useful in case you want to analyze a bunch of files in a directory. 
Monte Carlo data for example is usually split up over several thousand fits files. You specify a pattern 
to only take into account the filenames containing a certain pattern. In this example we only want to read 
the files ending with `_Events.fits.gz`. These are the usual filenames for simulated Telescope data written
by the simulation program Ceres.


        <container>
            <properties url="classpath:/default/settings.properties" />
      
            <!-- Pathes to the input files -->
            <property name="num_copies" value="2" />
        
            <property name="infile" value="file:/Users/kai/fact_phido/simulated/ceres/proton_klaus_9/" />
            <property name="drsfile" value="file:src/main/resources/testMcDrsFile.drs.fits.gz" />
            <property name="integralGainFile" value="classpath:/default/defaultIntegralGains.csv" />
        
            <stream id="fact:${copy.id}" class="fact.io.RecursiveDirectoryStream"
                    pattern="_Events.fits.gz" copies="${num_copies}" url="${infile}">
                <stream class="fact.io.FITSStream" id="_" limit="10"/>
            </stream>
        
            <!-- Description of the process and the corresponding stream -->
            <process id="2" input="fact:${copy.id}" copies="${num_copies}" >
        
                <stream.flow.Skip condition="%{data.EventNum} &lt; 0" />
                <fact.io.PrintKeys keys="MCorsikaEvtHeader.fTotalEnergy" />
        
                <include url="classpath:/default/mc/calibration_mc.xml" />
        
                <include url="classpath:/default/mc/extraction_mc.xml" />
        
                <include url="classpath:/default/mc/cleaning_mc.xml" />
        
            </process>
        </container>

Keep in mind that whenever you're using the `copies` parameter of a stream you have to write out your reuslts 
to a separate file for each thread. In other words you have to append `${copy.id}` to the filename you're writing to.
 
 


### Using  FileListMultiStreams

This multistream is useful for analyzing real data files from the telescope. It uses a .json file as a whitelist
which contains the paths to the data and drs files.

Here is an example .xml file.


        <property name="whitelist" value="some_list.json" />

        <property name="num_copies" value="2" />

        <service id="auxFileService" class="fact.auxservice.AuxFileService" auxFolder="file:/fact/aux/2013/10/12/" />


        <stream id="mystream:${copy.id}" class="fact.io.FactFileListMultiStream"
                url="file:${whitelist}" copies="${num_copies}" >
            <stream class="fact.io.zfits.ZFitsStream" id="_" limit="100"/>
        </stream>


        <process input="mystream:${copy.id}" copies="${num_copies}" >

            <fact.datacorrection.DrsCalibration key="Data" outputKey="DataCalibrated"/>
            <fact.features.source.SourcePosition outputKey="sourcePosition" auxService="auxFileService"/>

        </process>


The FactFileListMultiStream takes a json file of the form

      {
     "20131012_168":{
          "drs_path":"\/fact\/raw\/2013\/10\/12\/20131012_189.drs.fits.gz",
          "data_path":"\fact\/raw\/2013\/10\/12\/20131012_168.fits.gz"
          },
       "20131012_170":{
          ...
       }
     }

and creates a multistream for the files listed.

The `drsPathKey` and `dataPathKey` parameter define the names of the keys in your .json. So in the example above they
would need to be set to "drs_path" and "data_path" which are the default values. A key called `@drsFile` will
be injected into the DataStream by this multistream.

There a few things to consider when using the FileListMultiStream:

1. When you're using this kind of stream you *don't*  need to set the `url` parameter of the DrsCalibration processor.

2. Your whitelist can only contain files from *one single night* since the AuxfileService only watches files from a 
specific night.
