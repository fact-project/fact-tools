## Using Multistreams

You often want to analyze many files at once or sequentially without starting a new fact-tools instance for each
data .fits file. This is where multistreams come in handy.

### Using  FileListMultiStreams

You start a multistream by wrapping your input stream  (any kind of stream will do) in another multistream which handles 
the processing of multiple files. Heres an example .xml that can utilize multiple CPU cores by using the `copies` property.


As always the process needs be assigned to a specific data source. Since there are `num_copies` copies of the stream and
the process in this case, you need a unique `id` for each of these. You can access a specific copy by using the 
`${copy.id}` property.



        <property name="whitelist" value="some_list.json" />

        <property name="num_copies" value="2" />

        <service id="auxFileService" class="fact.auxservice.AuxFileService" auxFolder="file:/fact/aux/2013/10/12/" />


        <stream id="mystream:${copy.id}" class="fact.io.FactFileListMultiStream"
                listUrl="file:${whitelist}" copies="${num_copies}" >
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

## Things to watch out for

1. When you're using this kind of stream you *don't*  need to set the `url` parameter of the DrsCalibration processor.

2. Your whitelist can only contain files from *one single night* since the AuxfileService only watches files from a 
specific night.
