### Running the FACT Tools

The FACT Tools are started from the command line. They require an XML configuration
that specifies the processing steps to execute. Given some {{{processing.xml}}} 
setup, the FACT-Tools are started by issuing

      # java -jar fact-tools_latest.jar processing.xml

This will spawn a new Java instance with all the process definitions found in the
specified file.

Simple configuration file can look as following:


	<application>
	  <stream id="fact" class="fact.io.FITSStream" 
	  		url="classpath:/testDataFile.fits.gz"/>
	
	  <process input="fact">
	  		<PrintData/>
	  </process>
	</application>


Here a test file *testDataFile.fits.gz* was put in the archive. 
Any other *fits* file can be used here using *file:* keyword followed by a filepath.

On each data item from the FitsStream ``PrintData`` processor is applied.
