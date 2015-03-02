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
