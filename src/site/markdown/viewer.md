# The FACT Viewer

The largest component of the FACT tools is the standalone viewer for
inspecting FACT data files. Currently the viewer supports reading and
displaying FACT data events as well as exporting these events to
animated GIF files.

The following picture shows a screenshot of the FactViewer:
<div style="text-align: center;">
   <img src="images/fact-viewer-screen1.png" style="width:700px;" />
</div>

More features are to follow.
    
## Download of the Viewer

The current version of the FactViewer is `0.3.1-SNAPSHOT`. It can be found
as a standalone Java archive at:
<div style="text-align: center;">
   <a href="http://download.jwall.org/fact-tools/fact-tools-0.3.1-SNAPSHOT.jar">http://download.jwall.org/fact-tools/fact-tools-0.3.1-SNAPSHOT.jar</a>
</div>
    
## Running the Viewer

The viewer is provided as a standalone JAR archive that can be
executed with Java right away:

     # java -cp fact-tools-0.3.1-SNAPSHOT.jar fact.FactViewer
     
The exact name of the JAR archive may change according to the version.
For instructions to build the viewer from source, see the build section
below.

In order to view DRS calibrated data within the viewer, it needs to
be started with the FITS data file and the FITS DRS file as arguments,
for example:

     # java -cp fact-tools-0.3.1-SNAPSHOT.jar fact.FactViewer 20111126_042.fits.gz 20111126_034.drs.fits.gz


### Using the Viewer

Using the viewer is rather simple. Pixels on the camera hex map
can be selected by mouse click. By holding the Ctrl-Key, pixels can
be added/removed to/from the current selection.

When holding the Shift-Key and hovering over the time chart, the
corresponding slice of the event is displayed in the camera map.


#### Creating animated GIFs

All the export functions (to GIF, to PNG) are accessible by
right-clicking the camera hex map. This will bring up a context
menu with the list of available export commands.



## Building the Viewer

Building the viewer is rather simple and just requires invoking
maven with the *assembly* target:

     # cd fact-tools
     # mvn package
     
This will download all required dependencies, will compile the
viewer classes and will create a single, executable JAR archive
in the default maven output directory `target`.

After building the viewer can be started by running the executable
JAR archive file with Java:

    # java -cp target/fact-tools-0.3.1-SNAPSHOT.jar fact.FactViewer
    
Depending on the actual version of the fact-tools, the exact filename
of this JAR archive may slightly differ.
