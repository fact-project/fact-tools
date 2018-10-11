# Changelog for the fact-tools

# Version 1.0.3 -- 23.05.2018

* Enable to revert DRSCalibration
* `SourcePosition` and `CameraToEquatorial` now throw errors instead of closing the stream.
* Remove `SQLiteService`
* Use `AuxService` for erna xmls
* Enable setting drsfile to `@drsFile` to signal that the drsfile should be read from this key in the data item

# Version 1.0.2 -- 03.05.2018

* Fix exit code being 0 even if an error occured by updating to streams 1.0.5
* `InterpolatePixelArray` now also interpolates pixels containing `NaN` / `Infinity` values
* Bug fix for `Writer.testKeys`, that resultet in a wrong error message
* Implement skipping rows in the `HDUReader`
* Update all author metadata

# Version 1.0.1 -- 16.03.2018

* Add doi via zenodo
* Improve error handling of `FileListMultiStream` and `RecursiveDirectoryStream`

# Version 1.0.0 -- 08.03.2018

This is the first release since 0.17.2, with lots of additions, fixes, 
new features and breaking changes.

* Complete set of coordinate transformations
* Removal of source dependent features from the standard analysis
* Rotate camera coordinate system by 90° to have the following definition
  * When looking on the camera from the dish, x points right and y points up
* All outputkeys are snake_case
* New hdureader is hopefully able to read every fits and zfits file
* New CeresStream reads in Ceres RunHeader files to provide run level information for simulations
* 27s events are skipped in the standard analysis by default
* Correctly report fact-tools version on the command line, including git commit
* Writers check keys for nulls
* Writers write default keys if `keys` is not set, which are different for observations
and simulations
* The xml files for observations and simulations got merged
* The AuxService can take the base directory of the usual FACT aux structure now, e.g. `/fact/aux`  
* The hillas parameters got reimplemented, there is only one version of each parameter now.
* A new `GainService` provides the gains on base of the runs timestamp
* New gain file for the current simulations (ceres 12)

* Lots of other small fixes and improvements

Full list of merged PRs:

* 0fd6a6b9c Merge pull request #336 from fact-project/mc_gain_ceres_12
* 502807e8b Merge pull request #333 from fact-project/gain_service
* af23c2339 Merge pull request #327 from fact-project/change_coordinate_system
* 6ca1e4c37 Merge pull request #328 from fact-project/classpath
* d87fd5bf0 Merge pull request #334 from fact-project/remove_key_output
* 1a0e51348 Merge pull request #329 from fact-project/move_single_pulse
* de9ab4f37 Merge pull request #330 from fact-project/remove_fft
* ec792dd0e Merge pull request #332 from fact-project/fix_repeat_count
* 853146adf Merge pull request #321 from fact-project/bias_patch_272
* b0f218016 Merge pull request #324 from fact-project/fix_erna_xml
* 936862e8e Merge pull request #325 from fact-project/fix_viewer
* 0c1095329 Merge pull request #326 from fact-project/silence_streams
* 6b320c0a2 Merge pull request #318 from fact-project/single_analysis
* 9cefb8684 Merge pull request #316 from fact-project/consistency
* c38345402 Merge pull request #317 from fact-project/fix_merge_artifacts
* 6274d372e Merge pull request #315 from fact-project/rename_snake_case
* 8ba45a02c Merge pull request #314 from fact-project/remove_source_features
* 7b79f3faf Merge pull request #312 from fact-project/fix_fellwaker
* 56a6aae4f Merge pull request #311 from fact-project/optional_hdu
* aec556486 Merge pull request #308 from fact-project/remove_deprecated
* 7aa15e570 Merge pull request #310 from fact-project/runheader
* 46ca322b9 Merge pull request #237 from fact-project/ceresstream
* f9c0687dd Merge pull request #305 from fact-project/flag_27s
* 0573494ed Merge pull request #307 from fact-project/fix_pom_warning
* 91d9f61f4 Merge pull request #306 from fact-project/update_pom
* fbb661dca Merge pull request #300 from fact-project/refactor_hillas_parameters
* 32becfcee Merge pull request #292 from fact-project/cleanup_pixelsets
* 8d00d0b80 Merge pull request #303 from fact-project/sort_viewer_items
* 145f73022 Merge pull request #304 from fact-project/fix_exception_handling
* 075e84ad6 Merge pull request #302 from fact-project/fix_contains
* 811abc5f7 Merge pull request #296 from fact-project/remove_unused
* f82b07225 Merge pull request #299 from fact-project/remove_getters_setter
* b791b4207 Merge pull request #290 from fact-project/check_output_keys
* cb1b32da6 Merge pull request #297 from fact-project/fix_indentation
* 25d7bb033 Merge pull request #293 from fact-project/move_elementwise
* 368ca73d4 Merge pull request #295 from fact-project/refactor_pixelset
* 35b39bac7 Merge pull request #231 from fact-project/coordinate_trafos
* 8f1ece777 Merge pull request #288 from fact-project/fix_properties
* c13098c91 Merge pull request #285 from fact-project/fix_date_fitswriter
* c9c4e029a Merge pull request #264 from fact-project/fixhdureader
* 568daf212 Merge pull request #273 from fact-project/new_xmls
* 10bd9cdfd Merge pull request #278 from fact-project/throw_exceptions
* eb61e4d20 Merge pull request #282 from fact-project/remove_joda
* 5e07a5ebe Merge pull request #281 from fact-project/print_ft_version
* 07d0063e3 Merge pull request #275 from fact-project/update_readme
* e62d780bd Merge pull request #276 from fact-project/write_fits_in_xmls
* 73969134d Merge pull request #257 from fact-project/refactor_drs_time_calib
* 4682781b3 Merge pull request #271 from fact-project/fits_writer_header
* 6f0d4a2f5 Merge pull request #268 from fact-project/fix_delay_files
* 4744c848f Merge pull request #270 from fact-project/drs_output_key
* 4a1e4a815 Merge pull request #247 from fact-project/jebuss-adapt-auxservice-key-for-erna
* 8bbbf2124 Merge pull request #265 from fact-project/empty_cleaning
* 33a632b80 Merge pull request #258 from fact-project/write_timestamp
* bfdaa5803 Merge pull request #261 from fact-project/conc_core
* d79ce8125 Merge pull request #260 from fact-project/pixel_map_cr
* 73dfcf309 Merge pull request #259 from fact-project/drs_classpath
* 57219e491 Merge pull request #262 from fact-project/delay_files
* b04728691 Merge pull request #256 from fact-project/fix_244_hdureader
* 4fe25d1d7 Merge pull request #243 from fact-project/remove_tutorial_processors
* a50f361f0 Merge pull request #245 from fact-project/remove_unused_import
* 9413d0381 Merge pull request #241 from fact-project/refactorIO
* 8a96abd8d Merge pull request #234 from fact-project/hdu-reader
* 42ee44105 Merge pull request #235 from fact-project/hdu_reader_theap_fix
* c49314cc6 Merge pull request #218 from fact-project/aux_fixes
* bd787131a Merge pull request #228 from fact-project/table_names
* 46c819a70 Merge pull request #233 from fact-project/library_updates
* 2184ddec7 Merge pull request #226 from fact-project/fix_coordinate_check
* 94eafd1f3 Merge pull request #223 from fact-project/znaxis2_fix_219
* 63319e39a Merge pull request #201 from fact-project/AboveThresholdProcessor
* b744714e5 Merge pull request #211 from fact-project/fix_viewer_hdureader
* 984930a50 Merge pull request #180 from fact-project/fact-tools-streams-0.9.26
* 2b796474f Merge pull request #205 from fact-project/FixPackageLinePhotonStream
* f6e2b5a15 Merge pull request #202 from fact-project/create_photonstream_package_198
* 52033f5fd Merge pull request #177 from fact-project/hdu-reader
* 8a55db4fc Merge pull request #188 from fact-project/photonstream_to_num_photons_and_arrival_time
* 459bd6a44 Merge pull request #175 from fact-project/remove_test_output
* 1b2f47b94 Merge pull request #181 from fact-project/new_fits_reader_for_auxservice
* 44459d4bb Merge pull request #196 from fact-project/AlwaysExportTotPixelSet
* 5e1ca693e Merge pull request #194 from fact-project/FixJSONWriterNoControlOverDefaultKeys
* 6532357e8 Merge pull request #185 from fact-project/single_pulse_extractor_issue_183
* 1f1f91f21 Merge pull request #189 from fact-project/fix-download-link
* 2ec5214fe Merge pull request #186 from fact-project/add_some_numpy_to_xml
* 273de4098 Merge pull request #178 from fact-project/update_erna_xml

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
