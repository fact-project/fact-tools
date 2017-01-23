## Quickstart

So you [build](./build.html) or [downloaded](./download.html) the fact-tools. This means you should have a file 
called fact-tools-VERSION.jar. To run this you need an .xml file which provides the configuration of the stream.
You could use one of the examples from the examples folder. You simply run the programm by entering the following command 
into a prompt
 
    # java -jar target/fact-tools-VERSION.jar examples/viewer.xml
    
You should see the graphical user interface (GUI) popup after a couple of seconds.
Let's write our own .xml file for reading in a .fits file and printing out some information.

Every .xml needs some basic elements to work. Each .xml is wrapped in an outter `<container>` tag.
We then define a stream from a specific data source. In this case our `<stream ...>` is a `fact.io.FITSStream`.
The `<process>` is connected to the stream defined earlier via its `id` attribute. Inside the 
process we can define all the processors we want to use on the datastream. In this case we simply print
the data to the console output.

    <container>
    
        <stream id="some_stream" class="fact.io.FITSStream"  url="file:/path/to/file.fits"/>
    
        <process id="some_name" input="some_stream">
               <PrintData />
        </process>
    
    
    </container>

    
