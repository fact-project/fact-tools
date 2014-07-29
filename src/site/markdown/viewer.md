The FACT Viewer
--------------

The largest component of the FACT tools is the standalone viewer for
inspecting FACT data files. Currently the viewer supports reading and
displaying FACT data events.

The following picture shows a screenshot of the FactViewer:

<div id="textimg">
   <img src="images/fact-viewer-screen1.png" style="width:700px;" />
</div>


For more information on how you can display your specific data in the viewer see the
[progamming examples](programming_examples/displaying.html) about the viewer.
    

## Running the Viewer

The viewer part of the fact-tools .jar but it can be called just like any other Processor by calling

    <fact.ShowViewer key="DataCalibrated" range="25,250"/>


## Using the Viewer

Using the viewer is rather simple. Pixels on the camera hex map
can be selected by mouse click. By holding the Shift-Key, pixels can
be added/removed to/from the current selection. Some information like 
the Id and the coordinates of the selected Pixels will be displayed next to the camera view.

By right-clicking the camera image you can select between different color
schemes or export the camera image as a .png file.



