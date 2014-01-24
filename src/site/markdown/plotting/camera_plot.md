### Camera Plot

With the processors `CameraPlotter` you can simply plot arrays of size 1440 in the camera image. The values will be accumulated and the mean will be displayed. This example simply plots the reconstructed number of photons 
in the camera image.
This is quite useful if you want to see the distribution of your feature in the camera.


      ...
      
      <stream id="fact-data" ... />
      
      <process input="fact-data">
         ...
         
         <!-- photoncharge for the whole camera -->
         <fact.features.PhotonCharge key="DataCalibrated"  positions="risingedge" outputKey="photoncharge"/>

         <fact.plotter.CameraPlotter key="photoncharge" title="Plotty Plot" pixelSize="5.0" />

      </process> 
      ...

This will be the result. The parameter pixelSize specifies the size of one pixel. A larger number means a larger window.

<div style="text-align: center;">
   <img src="../images/camera_plot.png" style="width:400px;" />
</div>
