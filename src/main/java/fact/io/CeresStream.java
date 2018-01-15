package fact.io;

import fact.io.hdureader.*;
import fact.io.hdureader.FITSStream;
import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * When pointing this stream to the usual ceres output fits file ( XXX_Events.fits )
 * it will look for the XXX_RunHeaders.fits in the same folder and add the data from the corsika run header
 * and adds them to each data item.
 *
 * Use like that:
 *  <stream id="fact" class="fact.io.CeresStream" url="file:XXX_Events.fits"/>
 *
 * The stream will read the XXX_RunHeaders.fits in the same folder.
 */
public class CeresStream extends AbstractStream {

    private fact.io.hdureader.FITSStream fitsStream;
    private OptionalTypesMap<String, Serializable> ceresRunHeader;

    public CeresStream(SourceURL url){
        this.url = url;
    }

    @Override
    public void init() throws Exception {
        super.init();
        URL expandedURL;
        if (this.url.getProtocol().equals("classpath")){
            expandedURL = FITSStream.class.getResource(this.url.getPath());
        } else {
            expandedURL = new URL(this.url.getProtocol(), this.url.getHost(), this.url.getPort(), this.url.getFile());
        }

        fitsStream = new FITSStream(new SourceURL(expandedURL));
        fitsStream.init();

        Path path = Paths.get(expandedURL.getPath());
        String filename = path.getFileName().toString();

        if (!filename.contains("_Events")) {
            throw new IOException("Inputfile does not contain '_Events', cannot look for RunHeader file");
        }

        String runHeaderFileName = filename.replace("_Events", "_RunHeaders");
        Path runHeaderPath = Paths.get(path.getParent().toString(), runHeaderFileName);

        if (Files.notExists(runHeaderPath)) {
            throw new IOException("Run Header fits file not found for input file '" + path.toString() + "'");
        }

        HDU runHeaderHDU = FITS.fromPath(runHeaderPath)
                               .getHDU("RunHeaders")
                               .orElseThrow(() -> new IOException("RunHeader file '" + runHeaderPath.toString() + "' did not contain 'RunHeaders' HDU"));

        BinTableReader tableReader = BinTableReader.forBinTable(runHeaderHDU.getBinTable());
        ceresRunHeader = tableReader.getNextRow();
    }

    @Override
    public Data readNext() throws Exception {
        Data data = fitsStream.readNext();
        if (data == null){
            return null;
        }
        data.putAll(ceresRunHeader);
        return data;
    }
}
