package fact.io.hdureader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by mackaiver on 03/11/16.
 */
public class Fits {
    static Logger log = LoggerFactory.getLogger(Fits.class);

    private final int HDULIMIT = 1000;

    final URL url;
    private final List<HDU>  hdus = new ArrayList<>();

    //this is a mapping from extname
    Map<String, HDU> t = new HashMap<>();
    Map<HDU, Long> m = new HashMap<>();

    public HDU primaryHDU;

    public Fits(URL  url) throws IOException {
        this.url = url;
        DataInputStream stream = new DataInputStream(new BufferedInputStream(url.openStream()));

        Long positionInStream = 0L;
        try {
            while (hdus.size() < HDULIMIT) {
                HDU h = new HDU(stream);
                hdus.add(h);

                Long positionOfDataArea = positionInStream + h.size;
                h.get("EXTNAME").ifPresent(name -> {
                    t.put(name, h);
                    m.put(h, positionOfDataArea);
                });

                ByteStreams.skipFully(stream, h.offsetToNextHDU());
                positionInStream += h.offsetToNextHDU();

            }
        } catch (EOFException e){
            log.info("End of file reached for url: {}. A total of {} HDUs were found in the file", url, hdus.size());
        }

        primaryHDU = hdus.get(0);
    }

    /**
     * Each extension has a keyword called EXTNAME. This method returns the extension with the given name if it exists.
     *
     * @param extname the HDU to get.
     * @return the HDU with the passed EXTNAME value
     */
    public Optional<HDU> getHDU(String extname){
        return Optional.ofNullable(t.get(extname));
    }

    public InputStream getInputStreamForHDUData(String hduExtName) throws IOException {
        if(!t.containsKey(hduExtName)){
            throw new IOException("Extension with name " + hduExtName + " not found in file" );
        }
        return getInputStreamForHDUData(t.get(hduExtName));
    }

    public InputStream getInputStreamForHDUData(HDU h) throws IOException {
        InputStream stream = url.openStream();
        long skipped = stream.skip(m.get(h));
        if(m.get(h) != skipped){
            log.warn("Could not skip to data area.");
        }
        return stream;
    }
}
