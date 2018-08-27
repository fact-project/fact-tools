# FACT-Tools [![Build Status](https://travis-ci.org/fact-project/fact-tools.svg?branch=master)](https://travis-ci.org/fact-project/fact-tools) [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.1200066.svg)](https://doi.org/10.5281/zenodo.1200066)

The `FACT-Tools` are a collection of processors and stream implementations
for the [streams](http://www.jwall.org/streams/) framework to analyse the data of the [*First G-APD Cherenkov Telescope*](https://fact-project.org).

This README file provides a quick overview of the `FACT-Tools`.
Further information, including API docs, can be found on the [project web-page](http://sfb876.tu-dortmund.de/FACT)

The project is licensed under the GNU Public License, Version 3 (GPLv3).


## Download the latest release

You can download the `jar`-file of the latest release version on our 
[GitHub Release Page](https://github.com/fact-project/fact-tools/releases)


## Building the `FACT-Tools`


The `FACT-Tools` are provided as a standard Maven project.
They can be built by calling maven as follows:

```
$ mvn package
```

This will automatically download the required libraries defined as dependencies in the project definition, run the unit test suite and will create a final bundled Jar file that includes all required classes in the `target` directory.


## Running the `FACT-Tools`

The final Jar file is called `fact-tools-VERSION.jar`,
 where `VERSION` is the current version of the project.

Running the `FACT-Tools` requires a simple process graph definition as an XML file.

With an XML file at hand the process graph can be started by issuing

```
$ java -jar fact-tools-VERSION.jar /path/to/process.xml
```

Properties in the `XML` file can be overwritten on the commandline by providing them with `-Dproperty=value`:

```
$ java -jar fact-tools-VERSION.jar examples/viewer.xml -Dinfile=file:20131101_151.fits.fz
```

We also provide a python package including commandline-executables to make running the `FACT-Tools` on
large amounts of FACT files easier, it's called [erna](https://github.com/fact-project/erna)

## Examples

There are a number of example XML files in the `examples/` directory.


To perform the current FACT-Tools standard analysis and write image parameters to FITS files, run 

```
$ java -jar <jar> examples/stdAnalysis.xml -Dinfile=file:<datafile> -Ddrsfile=file:<drsfile> -Doutfile=file:<outputfile> -DauxFolder=file:<aux_dir>
```

To convert FACT rawdata zfits files to standard, uncompressed FITS, use:
```
$ java -jar <jar> examples/zfits2fits.xml -Dinfile=file:<datafile> -Doutfile=file:<outputfile>
```

To apply the DRS calibration and save calibrated time series in standard, uncompressed FITS, use:
```
$ java -jar <jar> examples/apply_drs_calibration.xml -Dinfile=file:<datafile> -Ddrsfile=file:<drsfile> -Doutfile=file:<outputfile>
```

To save number of photons and mean arrival times (DL1) to standard, uncompressed FITS, use:
```
$ java -jar <jar> examples/save_dl1.xml -Dinfile=file:<datafile> -Ddrsfile=file:<drsfile> -Doutfile=file:<outputfile>
```
