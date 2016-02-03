fact-tools, [Website](http://sfb876.tu-dortmund.de/FACT/)  
=============
[![Build Status](https://travis-ci.org/fact-project/fact-tools.svg?branch=master)](https://travis-ci.org/fact-project/fact-tools)

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

A Trabis instance is running. 

[![Build Status](https://travis-ci.org/fact-project/fact-tools.svg?branch=master)](https://travis-ci.org/fact-project/fact-tools)



Running the *fact-tools*
------------------------

The final Jar file is called `fact-tools-VERSION.jar`, where `VERSION` is the
current version of the project. Running the *fact-tools* requires a simple
process graph definition as an XML file.

With an XML file at hand the process graph can be started by issuing

     # java -jar fact-tools-VERSION.jar /path/to/process.xml
     
     
Versioning Scheme
---------------
Starting with version 0.6.5 we all solemly swear to abide to the following versioning scheme. All who fail to follow these rules will be forced
to buy a round of alcoholic beverages for everyone else.

* The master branch only contains stable versions and no SNAPSHOTS (stable in this case mean we are comfortable with publishing results and sharing the program with other people)
* Version numbers have to look like this:  0.X.YZ(-SNAPSHOT). Where X,Y,Z are natural numbers.
* An xml file written for version 0.X has to work for all versions 0.X.YZ
* Major version changes i.e from 0.X to 0.Z will be tagged for release
* For new experimental features use a feature branch. See [this tutorial](http://git-scm.com/book/en/Git-Branching-Basic-Branching-and-Merging) for more information.

You will receive weekly hugs and much respect from your fellow peers for writing unit tests and documentation. 
Especially for adding content to our ever growing [project website](http://sfb876.tu-dortmund.de/FACT/) by editing
the content under the src/site directory.
  

Orientation for Naming of Processors and Keynames
---------------
Starting with version 0.7.0 we try to introduce a naming convention (or only a orientation, no punishment for not following). Due to the fact that therefore the interface of processors will change, the adapting of older processors to this naming convention will be done over a longer time period. New processors should follow this naming convention:

* Processors should be structured in packages
* Processor names should very shortly describe their functionality
* inputKeys shall contain the targeted type of item, for example:
      - dataKey, for processors who works on the data array
      - showerKey, for processors who works on the cleaned shower pixels
      - cogxKey, for processors who needs the x coordinate of the cog (center of gravity)
      - ..., look at the interface of other processors for impression, try to use the same keyname, as other processors
* outputKeys:
     - in case there is only one output value, use outputKey:
     - in case there are more than one output value, create one outputKey for each value, for example:
     - m3lOutputKey, m3tOutputKey (processor M3Long)


The details of this naming convention are of course discussable, but we should stick to one convention.

