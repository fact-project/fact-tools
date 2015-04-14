package fact.io;

import com.google.gson.Gson;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.Stream;
import stream.io.multi.AbstractMultiStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Takes a json file of the form
 *
 * {"20131012_168":{
 *      "closest":"\/Users\/kaibrugge\/fact_phido\/raw\/2013\/10\/12\/20131012_189.drs.fits.gz",
 *      "earlier":"\/Users\/kaibrugge\/fact_phido\/raw\/2013\/10\/12\/20131012_108.drs.fits.gz",
 *      "later":"\/Users\/kaibrugge\/fact_phido\/raw\/2013\/10\/12\/20131012_189.drs.fits.gz",
 *      "night":"20131012",
 *      "data_path":"\/Users\/kaibrugge\/fact_phido\/raw\/2013\/10\/12\/20131012_168.fits.gz"
 *      },
 *  "20131012_170":{
 *      ...
 *   }
 * }
 *
 * and creates a multistream for the files listed.
 *
 * The 'strategy' parameter defines which .drsFile will be taken into account by injecting a key '@drsFile'
 * into the DataStream.
 *
 *
 * Created by mackaiver on 4/10/15.
 */
public class FactFileListMultiStream extends AbstractMultiStream {

    /**
     * Models the connection between a dataFile and a drsFile
     */
    class DataDrsPair{
        File drsFile;
        File dataFile;
        public DataDrsPair(String dataFile, String drsFile){
            this.dataFile = new File(dataFile);
            this.drsFile = new File(drsFile);
        }
    }


    public static final BlockingQueue<DataDrsPair> fileQueue = new LinkedBlockingQueue<>();


    @Parameter(required = true, description = "A file containing a json array of strings with the allowed filenames. "+
            "(excluding the possible suffix)")
    private SourceURL listUrl = null;

    @Parameter(required = false)
    private String strategy = "closest";

    //counts how many files have been processed
    private int filesCounter = 0;

    private AbstractStream stream;


    @Override
    public void init() throws Exception {
        if(!fileQueue.isEmpty()){
            log.debug("files already loaded");
            return;
        }

        HashMap<String, HashMap<String, String>> fileNamesFromWhiteList = new HashMap<>();
        if(listUrl !=  null){
            File list = new File(listUrl.getFile());
            Gson g = new Gson();
            fileNamesFromWhiteList = g.fromJson(new BufferedReader(new FileReader(list)), fileNamesFromWhiteList.getClass());
        }

        log.info("Loading files.");
        for (Map<String, String> m : fileNamesFromWhiteList.values()){
            fileQueue.add(new DataDrsPair(m.get("data_path"), m.get(strategy)));
        }
        log.info("Loaded " + fileQueue.size() + " files for streaming.");
        //super.init();
    }

    public void setStreamProperties(AbstractStream stream, DataDrsPair p) throws Exception {
        stream.setUrl(new SourceURL(p.dataFile.toURI().toURL()));
        log.info("Streaming file: " + stream.getUrl().toString());
        stream.init();
        //try to set drs paths. Only works if stream is a fact stream
        try{
            FactStream factStream = (FactStream) stream;
            factStream.setDrsFile(p.drsFile);
        } catch (ClassCastException e){
            //pass
            log.debug("Could not set drsPath because stream is not a FactStream");
        }
    }

    @Override
    public Data readNext() throws Exception {

        if (stream == null) {
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            DataDrsPair f = fileQueue.poll();
            if (f == null || f.dataFile == null || f.drsFile == null) {
                return null;
            }
            setStreamProperties(stream, f);
            filesCounter++;
        }

        Data data = stream.read();
        //check whether this stream has any data left and start a new stream if we necessary
        if (data != null) {
            return data;
        } else {
            stream.close();

            DataDrsPair f = fileQueue.poll();
            if (f == null || f.dataFile == null) {
                return null;
            }
            setStreamProperties(stream, f);

            data = stream.read();

            filesCounter++;
            return data;
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        log.info("In total " + filesCounter +  " files were processed.");
    }

    public void setListUrl(SourceURL listUrl) {
        this.listUrl = listUrl;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }
}
