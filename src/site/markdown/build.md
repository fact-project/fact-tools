Building the FACT Tools
=======================

The fact-tools are structured as a simple *Maven* module within the
fact-analysis project. Therefore, for building the tools, you have to
have the Apache Maven build-tool installed.

Apache *Maven* can be downloaded from [maven.apache.org](http://maven.apache.org)
or installed on Ubuntu systems using `apt-get install maven2`.


Building the FACT Tools
-----------------------

Building the *fact-tools* is straight forward: Simply checkout the
fact-tools code and enter the directory. By running the `mvn` command,
Maven will take care of the build:

     # cd fact-tools
     # mvn -DskipTests=true package
     
  This will download all required dependencies, will compile the
classes and will create a single, executable JAR archive in the
default maven output directory `target`. The `-DskipTests=true` flag
will skip all the junit tests. As there is currently no way of
distributing example DRS/Fits files for testing, these unit tests are
of interest for developers only.

The resulting file can be found in `target/fact-tools.jar` and contains
all the classes required to run the *fact-tools*.


Running the FACT Tools
----------------------

The *fact-tools* package that has been assembled by the Maven commands
as described above essentially contains two important components:

  1. The `FactViwer` application
  2. The *streams* framework with FACT processors.

The *FactViewer* is intended to inspect and display FITS files that
provide FACT data. The *FactViewer* can be started by running:

    # java -cp target/fact-tools.jar fact.FactViewer


The *streams* framework is intended to run XML process configurations
that have previously been defined. These XML process definitions may
contain data-preprocessing steps, image cleaning operators and multiple
other custom implementations that are to be applied to a stream of
FACT data items (events).

For running an XML configuration with the *streams* framework, simply
start the `fact-tools.jar` with the XML definition as parameter:

    # java -jar target/fact-tools.jar /path/to/config.xml