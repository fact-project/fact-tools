# FACT-Tools [![Build Status](https://travis-ci.org/fact-project/fact-tools.svg?branch=master)](https://travis-ci.org/fact-project/fact-tools)


The `FACT-Tools` are a collection of processors and stream implementations
for the [streams](http://www.jwall.org/streams/) framework to analyse the data of the *First G-APD Cherenkov Telescope*.

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
