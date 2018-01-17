package fact.io.hdureader;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * A FITS object containing all HDUs in a given file.
 * HDUs can be accessed by their name.
 * <p>
 * FITS f = FITS.fromPath(p)
 * HDU offsets = f.getHDU("ZDrsCellOffsets").orElseThrow(IOException::new);
 * BinTable binTable = offsets.getBinTable();
 * <p>
 * BinTables can also directly be accessed by name if they exist.
 * <p>
 * FITS.fromPath(p).getBinTableByName("Events")
 * .ifPresent(binTable -> {
 * //do something with bintable
 * })
 * <p>
 * Data from the BinTable can be read using the BinTableReader and ZFITSHeapReader classes.
 * <p>
 * FITS f = new FITS(u);
 * BinTable events = f.getBinTableByName("Events").orElseThrow(IOException::new);
 * <p>
 * for(OptionalTypesMap<String, Serializable> p : BinTableReader.forBinTable(events)){
 * assertTrue(p.containsKey("Data"));
 * }
 * <p>
 * <p>
 * Created by mackaiver on 03/11/16.
 */
public class FITS {
    private static Logger log = LoggerFactory.getLogger(FITS.class);


    private final URL url;
    private final List<HDU> hdus = new ArrayList<>();

    private Map<String, HDU> hduNames = new HashMap<>();

    //stores the primary HDU for convinience access from the outside.
    public HDU primaryHDU;

    /**
     * Creates a fits instance from a path object without throwing checked exceptions immediately.
     *
     * @param path The path to the fits file
     * @return a FITS object
     */
    public static FITS fromPath(Path path) {
        try {
            return new FITS(path.toUri().toURL());
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Creates a fits instance from a File object without throwing checked exceptions immediately.
     *
     * @param file the fits file
     * @return a FITS object
     */
    public static FITS fromFile(File file) {
        if (!file.canRead()) {
            throw new RuntimeException("File " + file.toString() + " is not readable");
        }
        try {
            return new FITS(file.toURI().toURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FITS(URL url) throws IOException {
        this.url = url;
        DataInputStream stream = getUnGzippedDataStream(url);
        long absoluteHduOffsetInFile = 0;

        try {
            //Since all file are finite this loop will end at some point. Here we disable inspection checks for the statement
            //noinspection InfiniteLoopStatement
            while (true) {
                HDU h = new HDU(stream, url, absoluteHduOffsetInFile);
                absoluteHduOffsetInFile += h.header.headerSizeInBytes + h.offsetToNextHDU();

                hdus.add(h);

                h.header.get("EXTNAME").ifPresent(name -> {
                    hduNames.put(name, h);
                });

                ByteStreams.skipFully(stream, h.offsetToNextHDU());
            }

        } catch (EOFException e) {
            primaryHDU = hdus.get(0);
            stream.close();

            log.debug("A total of {} HDUs were found in the file.", hdus.size());
        }
    }

    static DataInputStream getUnGzippedDataStream(URL url) throws IOException {
        BufferedInputStream stream = new BufferedInputStream(url.openStream());


        byte[] header = new byte[2];
        stream.mark(2);

        int read = stream.read(header);
        if (read != 2) {
            throw new IOException("Could not read the first 2 bytes from stream.");
        }

        //reset stream to start
        stream.reset();

        //check if matches standard gzip magic number
        if (isGzippedCompressed(header)) {
            log.debug("Getting gzipped stream");
            return new DataInputStream(new BufferedInputStream(new GZIPInputStream(stream)));
        } else {
            return new DataInputStream(new BufferedInputStream(stream));
        }
    }

    /**
     * Given the first 2 bytes of an item stream, this returns whether the stream is
     * gzipped or not
     *
     * @param header byte[] containing the first two bytes of any file/stream
     * @return true iff stream is gzipped.
     */
    static boolean isGzippedCompressed(byte[] header) {
        return header[0] == (byte) GZIPInputStream.GZIP_MAGIC && header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8);
    }

    /**
     * Each extension has a keyword called EXTNAME. This method returns the extension with the given name if it exists.
     *
     * @param extname the HDU to get.
     * @return the HDU with the passed EXTNAME value
     */
    public Optional<HDU> getHDU(String extname) {
        return Optional.ofNullable(hduNames.get(extname));
    }


    /**
     * Get the a mapping of HDU names to all HDU objects in the file.
     *
     * @return a map of names to HDUs
     */
    public Map<String, HDU> getHDUs() {
        return hduNames;
    }

    /**
     * Returns the binary data table which is in the HDU by the given name.
     * If the HDU exists and it contains a table.
     *
     * @param hduExtName the name of the hdu which contains the bintable
     * @return the bintable for the specified hdu
     */
    public Optional<BinTable> getBinTableByName(String hduExtName) {
        if (!hduNames.containsKey(hduExtName)) {
            return Optional.empty();
        }
        HDU hdu = hduNames.get(hduExtName);
        return Optional.ofNullable(hdu.getBinTable());
    }


    /**
     * Provides a datastream to the data area of the given hdu.
     * This method is useful when reading custom data extensions that are present in the fits file.
     *
     * @param hdu the HDU of interest.
     * @return a DataInputStream providing data from the data section of the given hdu.
     * @throws IOException in case the stream cannot be opened
     */
    public DataInputStream getInputStreamForHDUData(HDU hdu) throws IOException {
        DataInputStream stream = getUnGzippedDataStream(url);


        long bytesToSkip = 0;
        for (HDU h : hdus) {
            if (h.equals(hdu)) {
                bytesToSkip += h.header.headerSizeInBytes;
                break;
            }
            bytesToSkip += h.header.headerSizeInBytes + h.sizeOfDataArea();
        }


        long skipped = stream.skip(bytesToSkip);
        if (bytesToSkip != skipped) {
            log.error("Could not skip to data area. Skipped only {} instead of requested {} bytes.", skipped, bytesToSkip);
            throw new IOException("Could not skip to data area.");
        }

        return stream;
    }


}
