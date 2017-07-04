package fact.io;

import fact.io.hdureader.*;
import fact.io.hdureader.FITSStream;
import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.Serializable;
import java.net.URL;
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

    fact.io.hdureader.FITSStream fitsstream;
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

        fitsstream = new FITSStream(new SourceURL(expandedURL));
        fitsstream.init();

        Path path = Paths.get(expandedURL.getPath());
        String runHeaderFileName = path.getFileName().toString().replace("_Events", "_RunHeaders");

        Path runHeaderPath = Paths.get(path.getParent().toString(), runHeaderFileName);

        HDU runHeaderHDU = FITS.fromPath(runHeaderPath).getHDU("RunHeaders");
        BinTableReader tableReader = BinTableReader.forBinTable(runHeaderHDU.getBinTable());
        ceresRunHeader = tableReader.getNextRow();
    }

    @Override
    public Data readNext() throws Exception {
        Data data = fitsstream.readNext();
        if (data == null){
            return null;
        }
        data.putAll(ceresRunHeader);
        return data;
    }
}
