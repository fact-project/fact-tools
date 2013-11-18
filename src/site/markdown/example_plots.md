Plotting Processes
=================

While the fact tools are not supposed to replace any existing framework for plotting data points, it still provides some simple 
plotting functionality to get a quick overview of the data you're currently working on. Plotters can be used just like any other 
processor in the *fact-tools*. Simply add the line to youre .xml and the plotter will be called.
Processors providing plotting features will be located in the fact.plotter package.

### The EventViewer

To invoke the EventViewer use the `ShowImage` processor anywhere in your .xml file

      ...
      
      <stream id="fact-data" ... />
      
      <process input="fact-data">
            ...
            <!-- plot it -->
             <fact.image.ShowImage/>
            ...
      </process> 
      ...

This will open the EventViewer window. If you close it the it will open again when the next Event is being processed.
You can also click on 'Next' in the EventViewer window to analyze and view the next Event.

### Scatter Plot

A very simple processors is provided by `ScatterPlotter`, which can be used
to easily plot single values against each other. In this example we use the `MaxAmplitude` and the `MaxAmplitudePosition` processors
to calculate some supposedly interesting feature from the events. In the next step we calculate the mean values over each of the 1440
pixel in the camera to finally plot them in a scatter plot.

      ...
      
      <stream id="fact-data" ... />
      
      <process input="fact-data">
         
            <!-- calculate some interesting features -->
            <fact.features.MaxAmplitude key="DataCalibrated" outputKey="maxAmplitude"/>
            <fact.features.MaxAmplitudePosition key="DataCalibrated" outputKey="position"/>

            <!-- calculate the mean values -->
            <fact.statistics.ArrayMean key="maxAmplitude" outputKey="maxAmplitudeMean" />
            <fact.statistics.ArrayMean key="position" outputKey="positionMean" />
            
            <!-- and plot it -->
            <fact.plotter.ScatterPlotter xValue="positionMean" yValue="maxAmplitudeMean" 
                                                   color="#A23456" title="Hello im a plot!" />

      </process> 
      ...

During the processing a new window will pop up which is being filled with points during processing
<div style="text-align: center;">
   <img src="images/scatterplot.png" style="width:550px;" />
</div>




