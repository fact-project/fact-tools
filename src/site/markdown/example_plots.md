Plotting Processes
=================

While the fact tools are not supposed to replace any existing framework for plotting data points, it still provides some simple 
plotting functionality to get a quick overview of the data you're currently working on. Plotters can be used just like any other 
processor in the *fact-tools*. Simply add the line to your .xml and the plotter will be called.
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






