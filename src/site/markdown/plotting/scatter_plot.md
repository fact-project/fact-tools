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



During the processing a new window will pop up which is being filled with points during processing. The color of the points is specified by the `color` parameter in the .xml.


<div style="text-align: center;">
   <img src="../images/scatterplot.png" style="width:550px;" />
</div>