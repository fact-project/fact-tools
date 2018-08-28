# Performing a Standard Analysis

The standard analysis is meant to analyse raw data files (either from the telescope, called "observations", or from MC Simulations, called "simulations") to identify 
and parametrize images of air showers. The calculated parameters can be used used afterwards to perform the gamma-hadron 
classification, energy reconstruction, and the directional reconstruction.

Besides the effort to make the simulations as similiar as possible to the observations, 
here are still some differences that need special treatment. 
To do this, the `CREATOR` FITS Header tag is checked and if it contains `Ceres`,
simulations are assumed, else observations.

## Standard Analysis xml File

The standard analysis xml files is:

* `examples/stdAnalysis.xml`

The upper part defines the pathes to the different input files (raw data file, drs file, aux directory, gains, pixel delays) and
the url to the [default settings](./standardSettings.html) files (settings.properties).
Than the stream and the process are defined.

These files need to be set differently for observations and simulations.

For simulations, use the provided drs, gain and delay files:
```
<property name="drsfile" value="file:src/main/resources/testMcDrsFile.drs.fits.gz"/>
<property name="integralGainFile" value="classpath:/default/defaultIntegralGains.csv"/>
<property name="pixelDelayFile" value="classpath:/default/delays_zero.csv"/>
```

For observations use the appropriate drs file, usually the one taken direcly before
the observation you want to analyse, and the provided gain and delay files:
```
<property name="drsfile" value="file:/path/to/drs/file"/>
<property name="integralGainFile" value="classpath:/default/gain_sorted_20131127.csv" />
<property name="pixelDelayFile" value="classpath:/default/delays_lightpulser_20150217.csv" />
```

The process contains 6 different steps, each contained in a module xml file,
which can be found in the classpath and is thus included in the jar.
The classpath is `src/main/resources/...` in the repository
and you can use `classpath:/...` to access it in the xml.

The analysis modules are the same for simulations and observations and contain 
control flow based on the `CREATOR` FITS header tag to perform the necessary 
steps, which are different for simulations and observations.

The analysis results are stored to FITS with the outputkeys determined from
the settings.properties file and whether the analyzed file containes observations
or simulations.

####  `analysis/init.xml`

For both: 

* The total ratio of saturated slices on the raw timeseries data (the adc values) is
calculated, this is useful to skip 27s Events induced by our defective light pulser.

Only for observations: 

* The startcell information are stored for the processing of the following events
* The `UnixTimeUTC` tuple is converted to a timestamp

#### `analysis/calibration.xml`

For both:

* DRS amplitude calibration
* Spike removal
* Interpolation of broken pixels

Only for simulations:

* All keys for which it is necessary are remapped from softID order to chid order.

Only for observations:

* DRS time calibration
* Jump removal
 
#### `analysis/extraction.xml`

For both:

* Estimation of the number of cherenkov photons per pixel and their arrival times.
* Handling of saturated  pixels
* Interpolation of broken pixels for number of cherenkov photons and arrival times.
 
#### `analysis/cleaning.xml`
  
Only for observations:

* TwoLevelTimeNeighbor cleaning taking Ceta Tauri into account

Only for simulations:

* TwoLevelTimeNeighbor cleaning

For both:

* The number of pixels after the cleaning is calculated
 
#### `analysis/imageParameters.xml`

For both:

* All source independent parameters are calculated

#### `analysis/sourceDependentParameters.xml`

This file is not used in the standard analysis, but can be used
to calculate the source depentent parameters (e.g. alpha, disp, theta)
and to perforn a rudimentary source position estimation.
It is advised to not uses this and use the higher level machine learning
tools provided in the [classifier-tools](https://github.com/fact-project/classifier-tools).


## Differences between observed and simulated data 

* no timestamp is available for simulated data
* some keys have to be remapped from softID to chid order
* No jump removal or drs time calibration is applied for simulations
* the source position is not taken from auxiliary files, but from columns in the mc-fits files
* different sets of output keys are provided, the simulations contain additional simulation
  truths, the observations addiotional meta data.

## Customized Analysis

If you want to customize your analysis (for example change settings) you can start with the standard analysis files and 
extend them.
You can overwrite every property read from the settings.properties file by writing a 
`<property name="name" value="value" />`  line behind the `<properties url="classpath:/default/settings.properties" />`
line.

For example changing the cleaning level:

    [...]
    <properties url="classpath:/default/settings_mc.properties" />

    <property name="TwoLevelTimeNeighbor.coreThreshold" value="5.0" />
    <property name="TwoLevelTimeNeighbor.neighborThreshold" value="2.5" />
    <property name="TwoLevelTimeNeighbor.timeLimit" value="10" />
    <property name="TwoLevelTimeNeighbor.minNumberOfPixel" value="2" />
    [...]

You can also add own your processors to the process  (for example new parameter calculations):

    [...]
    <include url="classpath:/analysis/imageParameters.xml" /> 
    <fact.features.NewAwesomeParameter showerKey="shower" outputKey="awesomeParameter" />
    [...]


## Using only single Steps of the Standard Analysis

If you want to use only single steps of the standard analysis chain in your own xml file, you can just include the 
different xml files from the classpath:

    <include url="classpath:/analysis/calibration.xml" />

You need to consider only some points:

- You have to include all xml files up to the one you want to use (so cleaning.xml needs extraction.xml, calibration.xml, init.xml)
- You have to read in the properties file:

    <properties url="classpath:/default/settings.properties" />

- the different xml files have some dependency on properties:
 - calibration.xml: property `drsfile` and service `calibService` set
 - extraction.xml: properties `integralGainFile` and `pixelDelayFile` set as well as the `calibService`
 - cleaning.xml: [AuxService](../aux.html) has to be set
 - sourceDependentParameters.xml: [AuxService](../aux.html)
 
 
