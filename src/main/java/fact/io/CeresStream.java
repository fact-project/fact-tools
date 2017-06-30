package fact.io;

import fact.io.hdureader.*;
import fact.io.hdureader.FITSStream;
import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Created by Kai on 27.06.17.
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
        fitsstream = new FITSStream(url);
        fitsstream.init();

        Path path = Paths.get(url.getPath());
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
