# Changelog for the fact-tools

# Version 0.18.1 -- 26.05.2017

* The HDU reader (new fits stream implementation) now ignores 
erroneous tile headers that appear in some files.


# Version 0.18.0 -- 19.01.2017

* A new reader for FITS and ZFITS files was added.
* fact.io.zfits.ZFitsStream and fact.io.FITSStream are deprecated now in
favour of the new fact.io.hdureader.FITSStream
* add fact.utils.PhotonStream2ArrivalTime
* add fact.utils.PhotonStream2NumberOfPhotons

# Version 0.17.5 -- 18.01.2017

* Always export the list of saturated pixel IDs to the data item instead of exporting it only if the list is not empty. 

# Version 0.17.4 -- 18.01.2017

* No more default keys for the output writers (FITS and JSON). The former default keys are now among others set in 'src/main/resources/default/settings.properties'. 

# Version 0.17.3 -- 17.01.2017

* output window: Cut out artifacts of SinglePulseExtractor.
* add fact.utils.CastDoubleArrayToIntArray
* add fact.utils.ElementwiseMultiplyDoubleArray


# Version 0.17.2 -- 05.12.2016

* Reduced noise in `WaveformFluctuations`
* added pedestal parameters to stdAnalysis
* added WaveformFluctuations on shower pixel to the ouput of the standard analysis

## Version 0.17.1 -- 29.11.2016

* Fix a null pointer exception that was thrown in `FITSWriter.finish` in case no
data item was ever written

## Version 0.17.0 -- 21.11.2016

* Improvements on Single Pulse Extractor
   * No more static API, now several instances of the SinglePulseExtractor can run in parallel with different settings.
   * No more static (global) variable to steer the behaviour of the SinglePulseExtractor.
   * Reconstruction of amplitude time series from the photon stream (`fact.utils.ConvertSinglePulses2Timeseries`).
   * Added example XML `singlePeMinimalExample.xml`
   * Use Java 8
* Refactoring
  * Renamed `fact.statistics.TimerowFeatures` to `fact.statistics.TimeseriesFeatures`

## Version 0.16.2 -- 26.10.2016

* ZFitsStream
    * Fixed a bug that prevented some small files (more columns than rows) from being read
    * Fixed a bug that prevented parsing of floats in scientific notation in the header of fits files

## Version 0.16.1 -- 17.10.2016

* AuxFileService
    * This fixes a bug that appears when files with non parseable filenames appear in the aux directories.


## Version 0.16.0 -- 16.08.2016

* FITSWriter
  * Added new Processor to write data to FITS file: `fact.io.FITSWriter`
  * Files can be read by fact-tools
  * Can write out scalar values and 1d-Arrays of fixed length

* Feature Extraction
  * New Feature: fact.extraction.NeighborPixelDCF calculates the Descrete correlation function of neighboring pixels
  * renamed fact.extraction.MeanCorrelation to fact.extraction.NeighborPixelCorrelation
  * New features in fact.extraction.NeighborPixelCorrelation: standard deviation, kurtosis, skewness, min and max are calculated additionally

* Single Pulse Extractor
  * A robust, baseline independent, extractor for single pulses on a sampling time line. It assumes that all amplitude variations on the time line are caused by single pulses of a FACT SIPM and reconstructs the arrival time slices of these pulses.
  * SinglePulseExtractor (object) Calculates the arrival positions of pulses on a single time line.
  * SinglePulseExtraction (processor) Applies the SinglePulseExtractor to all pixels and calculates a list of single pulse arrival time slices for each time line of a pixel.
  * The algorithm was developed in a python toy simulation on https://github.com/fact-project/single_pulse_extractor


## Version 0.15.0 -- 23.05.2016

* Muon Analysis
  * New Processor `RingStandardDeviationWithThreshold` to calculate Standard Deviation of the ArrivalTime on MyonRing pixels above a threshold.
  * Update example xml for muon study (`examples/studies/muon_identification.xml`)
  * `fact.features.muon.HoughTransform` now uses a `PixelSet` for the `BestRingPixels`
  * Default outputkeys of `fact.features.muon.GaussianFit` and `fact.features.muon.CircularFit` now use CamelCase instead of snake_case.

* Improved error handling in source position calculation and AuxServices. The process now properly stops in case an IO related error occurs.

* New Features: Watershed
  * New package Watershed, containing ClusterFellwalker, ClusterArrivalTimes, FactCluster
  * ClusterFellwalker (Processor) creates new features based on a watershed algorithm called FellWalker. This algorithm groups camera pixels by their values for photoncharge or something else. From this clusters, new features based on number,
    content, shape and position in camera are extracted.
  * ClusterArrivalTimes (Processor) also creates new features, based on another algorithm (Region Growing). In this case, pixels are grouped by their arrival times. The extracted features are similar to ClusterFellwalker.
  * FactCluster (Object) holds all features and information about a cluster; used by ClusterFellwalker and ClusterArrivaltimes

* MotionCleaning
  * Idea for a cleaning: Select all pixels which show a strong increase in brightness over the time series in a time window from slice 50 to slice 120.
    This cleaning does not select shower as compact and exactly as standard cleanings!!! There are many more islands surviving the cleaning. Could be useful, but not for the standard analysis settings at the moment!!

* CleaningPerformance
  * Calculates performance values as precision, recall, accuracy true/false positive/negative for a cleaning.

* SmoothBell
  * Processor to smooths a camera image (not the time series of a pixel). Calculates the mean of six times the value of a pixel plus values of all neighbor pixels.

* MeanCorrelation
  * Bug fix: no more errors due to division by zero in case the time series contains zero.


## Version 0.14.1 -- 10.05.2016
* Bug fix for the calculation of the source position in Monte Carlos:
  * Corsika, Ceres and FACT-Tools are using different definitions of the Zd/Az coordinate systen. In FACT-Tools 0.14.0 we change to the definition used by Astropy, but a bug was left in the coordinate transformation for MCs. Now this transformation was changed to fit the definition used in Astropy
* Example XMLs to be used with the tourque/maui grid wrapper, called ERNA (https://github.com/fact-project/erna)

## Version 0.14.0 -- 03.05.2016

* Bug fix for the calculation of the source position (and position of stars in the camera):
  * There was an arcsin in the calculation of the az of a given ra/dec coordinate, causing the calculated az to be in -90 to 90 degree.
  * This was not a problem for Crab Nebula data (or at least for most of it, so that a clear signal could be achieved) but for Mrk 421 data. For this data, there was no signal of the source visible.
  * New calculation formulas are now implemented, which are using the atan2 method for this.
  * The new calculated coordinate transformation were checked against python implementation and also checked by Max Ahnen, who could clearly see a signal of Mrk 421 after the bugfix.
  * Corresponding tests were adapted.
* There is now also an example file for the erna std analysis (this is work in progress)


## Version 0.13.3 -- 06.04.2016

* new Processor: `fact.ApplyModel` which applies a .pmml model to data in the data item. The model can be written with SciKit learn.
* added several xml files, making use of the ApplyModel processor.
* MeasurePerformance now handles multiple uses of the same Processor correctly

## Version 0.13.2 -- 06.04.2016

* new Processor: `fact.extraction.TimeOverThresholdTL` allows to calculate the time over Threshold for a given window on the Timeline
* `fact.features.singlePulse.FWHMPulses` now uses Doubles instead of Ints and throws NaNs in case of out-of-boundary issues

## Version 0.13.1 -- 06.04.2016

* new Processor `fact.extraction.MeanCorrelation` to calculate the average covariance and correlation of the time series of neighboring pixels


## Version 0.13.0 -- 16.02.2016

* new package `fact.features.muon`
  * move `fact.features.MuonHoughTransform` to `fact.features.muon.HoughTransform`
  * new Processor `fact.features.muon.CircularFit` to fit a circle to the light distribution
  * new Processor `fact.feautures.muon.GaussianFit` to fit a gaussian to the radial light
  distribution
  * example xml to show how these work: examples/studies/muon_fitting.xml
* exceptions occuring in the functional tests are now logged
* new Processor `UnixTimeUTC2DateTime` to convert from a tuple of (seconds, microsends)
to a DateTime object
* new TypeAdapter in `JSONWriter` to write out `DateTime` objects as ISO string
* fix null-value handling for the `PixelSetAdapter` in JSONWriter

## Version 0.12.3

* `JSONWriter` now supports directly writing gzipped files with the
option `gzip="true"`

## Version 0.12.2

Changes:

* Add new  colormaps for the viewer. Viridis, Inferno, Magma ...
* Remove obsolete Processor called `JSONWriterExcludeKeys`



## Version 0.12.1
Other changes:

* Deleted unused xml files in project root.
* Fixed a bug in the SQLiteService which only allowed for data taken in January.
* Replaced empty test sqlite file with real one.

Changes in `fact.io.JSONWriter`

* `json` format by default, optional `jsonl` format by using `jsonl="true"` in the `xml`
* New `append` option, default is false, so existing files are overwritten.
* The `keys` key is now evaluated using the `stream.Keys` class, so glob patterns now work.
* Add flag `specialDoubleValuesAsString` to write special Double as strings for strict json compatability. See Issue #92 for more details. Via default special values are converted to `Infinity`, `-Infinity` and `NaN`.


No xmls in the example folder needed to be changed, although they are no behaving differently (output is `json`, not `jsonl` now).




## Version 0.11.2

Fixed a bug in the FactFileListMultiStream. The Wrapped stream wasnt initialized properly when it threw an exception during initialization.

## Version 0.11.1

Fixed the SqliteService. It now works on data that was not taken exclusively in january.

## Version 0.11.0

### Changes

The FileListMultiStream was rewritten. It wants a different kind of json file now. The .json files can be created from the runDB by using ERNA https://github.com/mackaiver/erna
It also tries to skip files that throw an IOexception.
The new syntax for the .json files is

      [
      {
       "drs_path":"\/fact\/raw\/2014\/10\/02\/20141002_193.drs.fits.gz",
       "data_path":"\/fact\/raw\/2014\/10\/02\/20141002_185.fits.fz",
       ...
       },
       {...}
       ]

### New Features
  Convenient stuff for simpler processing of data was added
  - The RecursiveDirectoryStream was rewritten. It now takes glob patterns to search for files below the given URL. This is useful for MC processing
  - The SqliteService was added to allow fetching of telescope drive related features form a SqLite DB which can  be generated by a python script which can be
    found here : https://github.com/mackaiver/erna


## Version 0.10.0

This versions contains the calibrationService branch and the PixelSetOverlay branch.

### Calibration Service

The new calibrationService can be used by other processors to access informations about the calibration values for the current event. At the moment it only offers the information which pixels are bad and which pixels can't be used for the cleaning process.

The calibrationService is now used in the Interpolation processors and there are some improvements (and bug fixing) done:
- The InterpolateBadPixel processor is renamed to InterpolateTimeLine
- A new InterpolatePhotondata processor is implemented (and used in the default xml files) which interpolates also photoncharge and arrivalTime of the bad pixels
- The bug, that also bad pixel could be used for interpolation, was fixed
- The interpolation processors now add PixelOverlaySets for the bad pixel to the data item

The calibrationService is also used in the BasicCleaning:
- If there are notUsable pixels (for example the broken drs board) they are now not added to the cleaned pixel set
- If there are notUsable pixels a PixelOverlaySet is added to the data item

How to adapt the xml files:
- If you are using the default xml files from the classpath, you only need to add the calibrationService to the xml file (see examples/example_process.xml)
- If you are using the InterpolateBadPixel processor, you have to rename it and adapt the giving parameter, see src/main/resources/default/data/calibration.xml for an example
 - maybe you want to add the new InterpolatePhotondata, see src/main/resources/default/data/extraction.xml for an example
- If you are using cleaning processors (TwoLevelTimeNeighbor, TwoLevelTimeMedian) you have to add the calibService to parameters of the processor, see src/main/resources/default/data/cleaning.xml for an example

### Bugfix in PatchJumpRemoval:
- fixed a bug, which caused the jump removal to remove only jumps up.

### Pixel Set Package
The new pixel set package allows to perform set operations (union, intersection, difference,...) on sets of pixels. These processors allow to take the set of pixels after cleaning and e.g. calculate the set of non shower pixel without broken pixels.
There is now also a pixelsets.Length processor, calculating the length of a pixelsets and replacing the NumberOfPixelInShower processor

### PixelSetOverlay instead of int array
All processors that work on a set of pixels or have a pixel set as result where changed such that:
- Instead of an int array a PixelSetOverlay is stored in the data item
- The so far used int array with cleaned pixels was removed.
- In all processors the key for a set of pixels was renamed from pixelSampleKey, shower, showerKey to pixelSetKey for consistency reasons
- pixelSetOverlay was moved from hexmap.ui.overlays to containers

### WaveformFluctuations with Pixel Set Option
The WaveformFluctuationsPixelSample processor is changed that way that it is possible to hand over a pixels set on which the calculations shall be performed.

### Miscellaneous changes:

- The impact parameter is now added to the outputfile in the standard mc analysis


## Version 0.9.7

Changes from version 0.9.6: fixed bug in HotColorMap, division by zero

## Version 0.9.0

These version contains several smaller developments:

SourcePosition operator:
- The operator now uses the closest strategy to find the tracking report from the aux file.

TwoLevelTimeNeighbor operator:
- There was a bugfix for the applyTimeNeighborCleaning function: Not only neighboring shower pixels,
 but all neighbor pixels were checked for the time neighbor cleaning.

maxnoe/risingedgepolynomfit branch:
- The rising edge branch was merged into the version 0.9.0:
 - the polynom fit is now calculated analytical
 - the whole drs time calibration is calculated in the unit slices (no converting to ns)
 - the interpolate function of the LinearTimeCorrectionKernel now uses a binary search
 - all changes improve the runtime

skipBrokenFiles branch:
- The skipBrokenFiles branch was merged into the version 0.9.0:
 - When using a RecursiveDirectoryStream, there is now a flag, to skip files which cannot be read in.
  If the flag is true, the whole process is not aborted anymore

settings.properties:
- The lower cleaning level (level 1: 5.5, level 2: 3.0) are now default for data and mc.

stdAnalysis:
- The lightpulser delay file is now used in the stdAnalysis

## Version 0.8.10 (tag icrc2015)

With this version the results of the analysis for the icrc 2015 can be reproduced.
Therefore a few changes were made in comparison to the previous version:

SourcePosition operator:
- The operator now uses the whole timestamp not only the seconds of the event time for calculating
 the source position and the correct tracking report
- The operator uses the earlier strategy to find the tracking report from the aux file. (This is for reproducibility,
 it will be changed in version 0.9.0 to the closest strategy)

cleaning.xml in the classpath:
- The position of zeta tauri was a little bit wrong (difference 0.7 arcsec)

viewer.xml:
- There is now the property auxFolder, to specify it via the command line

stdAnalysis:
- There is now the property auxFolder, to specify it via the command line
- The cleaning level were lowered
- The old delay file is used (This is for reproducibility, it will be changed in version 0.9.0)

## Version 0.8.8
Added a replacement process to measure the performance of individual Processors.
You can use it like this:

        <process class="fact.PerformanceMeasuringProcess" url="file:./measured_runtime.json" id="1" input="fact"
                warmupIterations="33">

## Version 0.8.6

A new tutorial package has been added. It contains some simple processors that hopefully help to understand the basic concepts
of the analysis.


## Version 0.8.0


The `ZFitsCalibration` processor does no longer exist. Its being handled by the `ZfitsStream` itsself.
For reading good old .fits files (or ceres output) you still need the `fact.io.FITSStream`.

To use any kind of data from an AuxFile you should now use the `AuxFileService` Service. (See the website for more details)

The DrsCalibration processor now resides in the `fact.datacorrection` package along with such processors as
DrsTimeCalibration and similar operations.

We completely changed the way the source position is calculated. As you can see above we now get the information from the
auxiliary files through the new AuxService. This fixes some bugs which would occur in some edge cases while reading aux data.

The SourcePosition operator now supports wobbled MonteCarlos (Ceres revision > 18159) and the output has been cleaned up.

The major steps of the standard analysis have been grouped into .xml files which reside in classpath of the project.
(you can find them under ./src/main/resources/default/...)

Cleaned up all the .xmls from the examples folder. All are checked by tests except for the GUI

Added alot more documentation to the website

We now support an early version of easy multistream handling to stream data from more than one file to your process.




## Version 0.7.9

Added processor to correct utc times with gps time correction files:
  `fact.filter.GpsTimeCorrection`


## Version 0.7.7

Removed dependencies to old streams versions by removing all code depending on the streams-dashboard.


## Version 0.7.6

 The new DRS4 time calibration processor has been improved
 `fact.datacorrection.DrsTimeCalibration`

 Both the old and the new DRS4 time calibration processors now use the new std
 input file containing the measured DRS4 time calibration constants.
 `fact-tools/src/main/resources/long_term_constants_median.time.drs.fits`

## Version 0.7.5

 The Camera and Plotting Windows in the viewer can now also show Arrays of types other than double.
 We also implemented a new DrsTimeCalibration which you call as follows:

           <fact.datacorrection.DrsTimeCalibration
                   dataKey="DataCalibrated"
                   outputKey="DataCalibrationNeu"
           />


## Version 0.7.0

The Version 0.7.0 was developed in the extractAndCleaningDevelop branch. There are mainly changes in the extraction and in the cleaning package, which increased the quality of the standard analysis.
Also some internal code restructuring were done. The Analysis for the General Meeting 2015 were performed with this version.

Extraction package:

- new class BasicExtraction
- performs the standard extraction (max Amplitude, position half height, integration)
- serves also as a parent class for extraction processors (provides some standard methods)
- implementing methods checkWindow and getValidWindow in Utils
- restructuring of the code, making use of Utils.checkWindow and Utils.getValidWindow => some bugfixes, specialy in cases at the egde of search windows
- restructuring of the code of the RisingEdge Processors, making use of Utils.checkWindow and Utils.getValidWindow
- all extraction processors now take a startvalue and a range for search windows!

Cleaning Development:
- Restructering of the Cleaning package
- new class BasicCleaning
- contains the methods used in the two level cleaning and the removeStarCluster method
- new processor TwoLevelTimeNeighbor (the cleaning method now used in the standard analysis)
- Renaming of CoreNeighborClean to TwoLevelTimeMedian

feature package:
- code Restructuring of several processors
- renaming of some keys in several processors, to be consistent with the "key-naming"-convention (see Readme):
- M3Long
- NumberOfIslands
- Leakage
- ShowerSlope
- new processor TimeGradient
- Reworked of source.Theta:
- bugfix in the calculation
- therefore more keys are now needed

Examples folder:
 - deleting obsolet xml-files
 - Restructuring the directory
 - only 4 xml files in the top directory:
 - all 4 xml files should work, maybe pathes has to be changed, according to the location of input files
 - viewer (performs calibration, extraction and cleaning) and opens the fact-tools viewer (for data-gz-files, data-fz-files and mc-files)
 - example_process (as a starting point for developing your own xml file). Opens an input stream and only performs a calibration on the data
 - new folders:
 - stdAnalysis folder:
 - xml Files for the actual standard analysis (for data-gz-files, data-fz-files and mc-files)
 - all files should work, maybe pathes has to be changed, according to the location of input files
 - studies folder:
 - all other xml files in the previous example folder
 - changed the interface of the processors, according to the changes done for this version. Not tested.
