package fact.io;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import stream.Data;
import stream.annotations.Parameter;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.multi.AbstractMultiStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Takes a json file of the form
 * <p>
 * [
 * {
 * "drs_path":"\/fact\/raw\/2014\/10\/02\/20141002_193.drs.fits.gz",
 * "data_path":"\/fact\/raw\/2014\/10\/02\/20141002_185.fits.fz",
 * ...
 * },
 * {...}
 * ]
 * <p>
 * and creates a single stream for the files listed. The dictionaries in the json may of course contain
 * more keys.
 * The 'drsPathKey' and `dataPathKey` parameter define the names of the keys to the file paths. So in the example above they
 * would need to be set to "drs_path" and "data_path" which are the default values. A key called '@drsFile' will
 * be injected into the DataStream by this multistream. That means when you're using this stream you don't need to set the
 * `url` parameter of the DrsCalibration processor. If the underlying stream throws an IOexception in case of missing files,
 * the next file will be tried when the skipErrors flag is set.
 * <p>
 * Created by mackaiver on 4/10/15.
 */
public class FactFileListMultiStream extends AbstractMultiStream {

    private DataDrsPair dataDrsPair;

    public FactFileListMultiStream(SourceURL url) {
        super(url);
    }

    /**
     * Models the connection between a dataFile and a drsFile
     */
    class DataDrsPair {
        final File drsFile;
        final File dataFile;

        public DataDrsPair(String dataFile, String drsFile) {
            this.dataFile = new File(dataFile);
            this.drsFile = new File(drsFile);
        }
    }


    public final BlockingQueue<DataDrsPair> fileQueue = new LinkedBlockingQueue<>();


    @Parameter(required = true, description = "A file containing a json array of dicts with the paths to the files.")
    private SourceURL url = null;

    @Parameter(required = false, description = "Flag indicating whether next file should be tried in case of errors in underlying stream.", defaultValue = "false")
    private boolean skipErrors = false;

    @Parameter(required = false, defaultValue = "drs_path")
    private String drsPathKey = "drs_path";

    @Parameter(required = false, defaultValue = "data_path")
    private String dataPathKey = "data_path";

    //counts how many files have been processed
    private int filesCounter = 0;

    private AbstractStream stream;


    /**
     * Read the json file provided by the url parameter and build a queue of File objects to be analyzed
     *
     * @throws Exception might be thrown in case the json cannot be read.
     */
    @Override
    public void init() throws Exception {
        if (!fileQueue.isEmpty()) {
            log.debug("files already loaded");
            return;
        }

        ArrayList<HashMap<String, String>> fileNamesFromWhiteList = new ArrayList<>();
        if (url != null) {
            File list = new File(url.getFile());
            Gson g = new Gson();
            //use guave typetoken trickery to avoid unchecked typecasts.
            Type listType = new TypeToken<ArrayList<HashMap<String, String>>>() {
            }.getType();
            fileNamesFromWhiteList = g.fromJson(new BufferedReader(new FileReader(list)), listType);
        }

        log.info("Loading files.");
        for (Map<String, String> m : fileNamesFromWhiteList) {
            if (m.get(dataPathKey) == null || m.get(drsPathKey) == null) {
                log.error("Did not find the right data in the provided whitelist .json");
                throw new IllegalArgumentException("Did not find the right data in the provided whitelist .json");
            }
            fileQueue.add(new DataDrsPair(m.get(dataPathKey), m.get(drsPathKey)));
        }
        log.info("Loaded " + fileQueue.size() + " files for streaming.");
        //super.init();
    }


    @Override
    public Data readNext() throws Exception {
        try {
            if (stream == null) {
                stream = (AbstractStream) streams.get(additionOrder.get(0));
                dataDrsPair = fileQueue.poll();
                if (dataDrsPair == null) {
                    return null;
                }
                stream.setUrl(new SourceURL(dataDrsPair.dataFile.toURI().toURL()));
                stream.init();

                filesCounter++;
            }

            Data data = stream.read();

            //check whether this stream has any data left and start a new stream if necessary
            if (data == null) {
                if (fileQueue.isEmpty()) {
                    return null;
                }
                stream.close();

                dataDrsPair = fileQueue.poll();
                stream.setUrl(new SourceURL(dataDrsPair.dataFile.toURI().toURL()));
                stream.init();

                data = stream.readNext();
                data.put("@drsFile", dataDrsPair.drsFile);
                filesCounter++;
            }

            data.put("@drsFile", dataDrsPair.drsFile);
            return data;

        } catch (IOException e) {
            log.info("File: " + stream.getUrl().toString() + " throws IOException.");

            if (skipErrors) {
                log.info("Skipping broken files. Continuing with next file.");
                e.printStackTrace();
                stream = null;
                return this.readNext();
            } else {
                log.error("Stopping stream because of IOException");
                stream.close();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        super.close();
        log.info("In total {} files were processed.", filesCounter);
    }

    public void setUrl(SourceURL url) {
        this.url = url;
    }

    public void setDrsPathKey(String drsPathKey) {
        this.drsPathKey = drsPathKey;
    }

    public void setDataPathKey(String dataPathKey) {
        this.dataPathKey = dataPathKey;
    }

    public void setSkipErrors(boolean skipErrors) {
        this.skipErrors = skipErrors;
    }

}
