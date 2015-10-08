### Running the FACT Tools

The FACT Tools are started from the command line. They require an XML configuration
that specifies the processing steps to execute. Given some {{{processing.xml}}} 
setup, the FACT-Tools are started by issuing

      # java -jar fact-tools_latest.jar processing.xml

This will spawn a new Java instance with all the process definitions found in the
specified file.

Simple configuration file can look as following:

```xml
<application>
  <stream id="fact" class="fact.io.FitsStream" 
  		url="classpath:/testDataFile.fits.gz"/>

  <process input="fact">
  		<PrintData/>
  </process>
</application>
```

On each data item from the FitsStream ``PrintData`` processor is applied.