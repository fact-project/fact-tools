# The FACT Viewer

The largest component of the FACT tools is the standalone viewer for
inspecting FACT data files. Currently the viewer supports reading and
displaying FACT data events as well as exporting these events to
animated GIF files.

The following picture shows a screenshot of the FactViewer:

<div style="text-align: center;">
   <img src="images/fact-viewer-screen1.png" style="width:700px;" />
</div>


More features to follow.
    

## Running the Viewer

The viewer part of the fact-tools .jar but it can be executed as a standalone program by Java:

     # java -cp fact-tools-{VERSION}.jar fact.FactViewer <path_to_fits>
     
The exact name of the JAR archive may change according to the version.
For instructions to build the viewer from source, see the build section.


## Using the Viewer

Using the viewer is rather simple. Pixels on the camera hex map
can be selected by mouse click. By holding the Ctrl-Key, pixels can
be added/removed to/from the current selection.

When holding the Shift-Key and hovering over the time chart, the
corresponding slice of the event is displayed in the camera map.


#### Creating animated GIFs

All the export functions (to GIF, to PNG) are accessible by
right-clicking the camera hex map. This will bring up a context
menu with the list of available export commands.
