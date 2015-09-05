## Handling auxiliary data

Its a common task to use some additional sensor data during your analysis. This means adding more data to your
stream from the outside. In case of the FACT telescope this data comes from various sources like weather stations or
dust measurements and is written to files during data taking. 

### Reading Aux Files 

In case you want to read the data from files you can use a class called AuxFileService.
In your .xml you create an AuxService by adding the following line before the `process` tag opens.


        <service id="horst" class="fact.auxservice.AuxFileService" auxFolder="file:/fact/aux/2013/10/12/" />


The `auxFolder` url has to point to a directory containing the data for *one* night.
The file names of the auxfiles are expected to adhere to the usual naming conventions of the form `YYYYMMDD.SERVICE_NAME.fits`

If you want to use the service inside of your processor you simply add the service as a .xml parameter.

        @Parameter(required = false, description = "Name of the service that provides aux files")
        private AuxiliaryService auxService;

To get a data point from the service you need to specify a timestamp or take the one from the data event you're currently
analysing.

        AuxPointStrategy earlier = new Earlier();
        AuxPoint sourcePoint = auxService.getAuxiliaryData(AuxiliaryServiceName.DRIVE_CONTROL_SOURCE_POSITION, timeStamp, earlier);

Since sensor data can be written at different time intervals and frequencies we need to specify some strategy
for fetching the data. E.g. get the nearest point written before the given timestamp. For details about strategies which are already
implemented check the [JavaDocs](apidocs/index.html).

To get specific data from an AuxPoint you need to know the name of the key your data has in the original auxiliary file.
So for example if you want to get the right ascension of your source in the sky you simply call the appropriate method
on your AuxPoint

        double ra = sourcePoint.getDouble("Ra_src")

And then you call the processor from your .xml like this

        <fact.somePackage.someProcessor auxService="horst"/>


For some more general information about services and streams read the [Tech Report](./ressources.html) by C. Bockermann
