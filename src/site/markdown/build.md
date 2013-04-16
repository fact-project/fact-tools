Building the FACT Tools
=======================

  The fact-tools are structured as a simple *Maven* module within the
fact-analysis project. Therefore, for building the tools, you have
to have the Apache Maven build-tool installed.

  Apache Maven can be downloaded from [maven.apache.org](http://maven.apache.org).




Building the Viewer
-------------------

  The largest component of the FACT tools is the standalone viewer
for inspecting FACT data files. Building the viewer is rather simple
and just requires invoking maven with the *assembly* target:

     # cd fact-tools
     # mvn assembly:assembly
     
  This will download all required dependencies, will compile the
viewer classes and will create a single, executable JAR archive
in the default maven output directory `target`.

  After building the viewer can be started by running the executable
JAR archive file with Java:

    # java -cp target/FactTools.jar fact.FactViewer
    
  Depending on the actual version of the fact-tools, the exact filename
of this JAR archive may slightly differ.
