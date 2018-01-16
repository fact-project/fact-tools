# Performing a Standard Analysis

The standard analysis is meant to analyse raw data files (either from the telescope or from MC Simulations) to identify 
and parametrize images of air showers. The calculated parameters can afterwards used to perform a gamma-hadron 
classification, a energy reconstruction and an unfolding of the energy spectrum

Because there are still some semantic differences between the measured and the simulated
data we have to treat each case seperrated. 

## Standard Analysis xml Files

The standard analysis xml files are:

__examples/stdAnalysis/data/analysis.xml:__

> Analysis of real data files in unzipped, or gzipped .fits file format

> If you want to read .zfits files you need to use the `fact.io.zfits.ZFitsStream` 





__examples/stdAnalysis/mc/analysis_mc.xml:__ 

> Same as above except for simulated data 


The two .xml files follow the same structure, the differences are explained in the next subsection.

The upper part defines the pathes to the different input files (raw data file, drs file, tracking file, source file) and
the url to the [default settings](./standardSettings.html) files (settings.properties,integralGainFile,pixelDelayFile).
Than the stream and the process is defined.

The process contains of 6 different steps, each with an own xml file, which can be found in the classpath (src/main/resources/...):

__/default/data/prevEventAndSkip.xml:__
 > The startcell information are stored for the processing of the following events and all non data trigger are skipped
 
__/default/data/calibration.xml:__
 > Several Calibration tasks are performed (Drs calibration, Jump- and Spike removal, Drstime calibration, Bad pixel interpolation )
 
 
__/default/data/extraction.xml:__
 > The number of cherenkov photons per pixel and their arrival time are extracted. Also the saturation handling is performed
 
__/default/data/cleaning.xml:__
 > The TwoLevelTimeNeighbor cleaning is performed
 
__/default/data/parameterCalc.xml:__
 > All source independent parameters are calculated (see list of parameters)
 
__/default/data/sourceParameter.xml:__
 > All source dependent parameters are calculated (see list of parameters)
 

## Differences between real- and simulated data 

Differences  simulated data (monte carlos) vs measured data:

- the settings file for the mc and the defaultIntegralGain file is used
- for each step the corresponding mc version of the xml files is included
- the prevEventsAndSkip file is not used
- there is no jump removal and drs time calibration for mc
- there is no correct pixel delays for the mc
- the source position is not calculated from auxiliary files, but from columns in the mc-fits files

## Customized Analysis

If you want to customize your analysis (for example change settings) you can start with the standard analysis files and 
extend them. You can overwrite every property read from the settings.properties file, by writing a 
`<property name="name" value="value" />`  line behind the `<properties url="classpath:/default/settings_mc.properties" />`
line:

For example changing the cleaning level:

    [...]
    <properties url="classpath:/default/settings_mc.properties" />

    <property name="twoLevelTimeNeighbor_coreThreshold" value="5.5" />
    <property name="twoLevelTimeNeighbor_neighborThreshold" value="3" />
    <property name="twoLevelTimeNeighbor_timeLimit" value="10" />
    <property name="twoLevelTimeNeighbor_minNumberOfPixel" value="2" />
    [...]

You can also add own your processors to the process  (for example new parameter calculations):

    [...]
    <include url="classpath:/default/mc/parameterCalc_mc.xml" /> 
    <fact.features.NewAwesomeParameter showerKey="shower" outputKey="awesomeParameter" />
    [...]


## Using only single Steps of the Standard Analysis

If you want to use only single steps of the standard analysis chain in your own xml file, you can just include the 
different xml files from the classpath:

    <include url="classpath:/default/data/calibration.xml" />

You need to consider only some points:

- You have to include all xml files up to the one you want to use (so cleaning.xml needs extraction.xml, calibration.xml, prevEventAndSkip.xml)
- You have to read in the properties file:

    <properties url="classpath:/default/settings.properties" />

- the different xml files have some dependency of setted properties:
 - prevEventAndSkip.xml: no dependency
 - calibration.xml: property "drsfile" set
 - extraction.xml: property "integralGainFile" set, for real data files: property pixelDelayFile set
 - cleaning.xml: for real data files: the [AuxService](../aux.html) has to be set (to handle Zeta Tauri)
 - sourceParameter.xml: for real data files: the [AuxService](../aux.html) has to be set
 
 
