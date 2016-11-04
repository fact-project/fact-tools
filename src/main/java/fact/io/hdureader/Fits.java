package fact.io.hdureader;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by mackaiver on 03/11/16.
 */
public class Fits {
    private static Logger log = LoggerFactory.getLogger(Fits.class);

    private final int HDULIMIT = 1000;

    private final URL url;
    private final List<HDU>  hdus = new ArrayList<>();

    //this is a mapping from extname
    private Map<String, HDU> hduNames = new HashMap<>();

    public HDU primaryHDU;


    public Fits(URL  url) throws IOException {
        this.url = url;
        DataInputStream stream = getDecompressedDataStream(url.openStream());

        try {
            while (hdus.size() < HDULIMIT) {
                HDU h = new HDU(stream);
                hdus.add(h);


                h.get("EXTNAME").ifPresent(name -> {
                    hduNames.put(name, h);
                });

                ByteStreams.skipFully(stream, h.offsetToNextHDU());

            }
        } catch (EOFException e){
            log.info("End of file reached for url: {}. A total of {} HDUs were found in the file", url, hdus.size());
        }

        primaryHDU = hdus.get(0);
    }

    private DataInputStream getDecompressedDataStream(InputStream stream) throws IOException {

        byte[] header = new byte[2];
        stream.mark(2);

        int read = stream.read(header);
        if(read != 2){
            throw new IOException("Could not read the first 2 bytes from stream.");
        }

        //reset stream to start
        stream.reset();

        //check if matches standard gzip magic number
        if(isGzippedCompressed(header)) {
            log.info("Getting gzipped stream");
            return new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
        }else {
            return new DataInputStream(new BufferedInputStream(stream));
        }
    }

    /**
     * Given the first 2 bytes of an input stream, this returns whether the stream is
     * gzipped or not
     * @param header byte[] containing the first two bytes of any file/stream
     * @return true iff stream is gzipped.
     */
    public static boolean isGzippedCompressed(byte[] header){
        return header[0] == (byte) GZIPInputStream.GZIP_MAGIC  && header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
    }

    /**
     * Each extension has a keyword called EXTNAME. This method returns the extension with the given name if it exists.
     *
     * @param extname the HDU to get.
     * @return the HDU with the passed EXTNAME value
     */
    public Optional<HDU> getHDU(String extname){
        return Optional.ofNullable(hduNames.get(extname));
    }

    public InputStream getInputStreamForHDUData(String hduExtName) throws IOException {
        if(!hduNames.containsKey(hduExtName)){
            throw new IOException("Extension with name " + hduExtName + " not found in file" );
        }
        return getInputStreamForHDUData(hduNames.get(hduExtName));
    }

    /**
     *
     * @param hdu
     * @return
     * @throws IOException
     */
    public DataInputStream getInputStreamForHDUData(HDU hdu) throws IOException {
        DataInputStream stream = getDecompressedDataStream(url.openStream());


        long bytesToSkip = 0;
        for(HDU h : hdus){
            if(h.equals(hdu)){
                bytesToSkip += h.headerSizeInBytes;
                break;
            }
            bytesToSkip += h.headerSizeInBytes + h.sizeOfDataArea();
        }


        long skipped = stream.skip(bytesToSkip);
        if(bytesToSkip!= skipped ){
            log.error("Could not skip to data area. Skipped only {} instead of requested {} bytes.", skipped, bytesToSkip);
            throw new IOException("Could not skip to data area.");
        }

        return stream;
    }
}
