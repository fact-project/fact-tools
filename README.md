fact-tools
==========

The *fact-tools* are a collection of processors and stream implementations
for the [streams](http://www.jwall.org/streams/) framework. The tools are
designed to allow for the definition of streaming processes to process data
produced by the [FACT telescope](http://www.isdc.unige.ch/fact/).

This README file provides a quick overview of the *fact-tools*. Further
information can be found on the project web-page at

	http://sfb876.tu-dortmund.de/FACT/


The project is licensed under the Gnu Public License, Version 3 (GPLv3).
The license text is provided along with the sources.




Building the *fact-tools*
-------------------------

The *fact-tools* are provided as a standard Maven project. They can be
built by calling maven as follows:

     # mvn package

This will automatically download the required libraries defined as dependencies
in the project definition and will create a final bundled Jar file that
includes all required classes.

Continous Integration
-------------------------

A buildbot is running at [drone.io](https://drone.io/bitbucket.org/cbockermann/fact-tools) to provide some continuous integration services.
The [Website](http://www.isdc.unige.ch/fact/)  will also be rebuild automatically. So any changes to the *src/site/* directory will 
be visible on the web after a few minutes. At the moment a complete build takes about 10 to 15 minutes.

[![Build Status](https://drone.io/bitbucket.org/cbockermann/fact-tools/status.png)](https://drone.io/bitbucket.org/cbockermann/fact-tools/latest)





Running the *fact-tools*
------------------------

The final Jar file is called `fact-tools-VERSION.jar`, where `VERSION` is the
current version of the project. Running the *fact-tools* requires a simple
process graph definition as an XML file.

With an XML file at hand the process graph can be started by issuing

     # java -jar fact-tools-VERSION.jar /path/to/process.xml
