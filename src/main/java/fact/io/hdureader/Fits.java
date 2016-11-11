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


    private final URL url;
    private final List<HDU>  hdus = new ArrayList<>();

    private Map<String, HDU> hduNames = new HashMap<>();

    public HDU primaryHDU;


    public Fits(URL  url) throws IOException {
        this.url = url;
        DataInputStream stream = getDecompressedDataStream(url);
        long absoluteHduOffsetInFile = 0;

        try {
            while (true) {
                HDU h = new HDU(stream, url, absoluteHduOffsetInFile);
                absoluteHduOffsetInFile += h.headerSizeInBytes + h.offsetToNextHDU();

                hdus.add(h);

                h.get("EXTNAME").ifPresent(name -> {
                    hduNames.put(name, h);
                });


                ByteStreams.skipFully(stream, h.offsetToNextHDU());

            }
        } catch (EOFException e){
            log.info("A total of {} HDUs were found in the file", url, hdus.size());
        }

        primaryHDU = hdus.get(0);
    }

    static DataInputStream getDecompressedDataStream(URL url) throws IOException {
        InputStream stream = url.openStream();

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


    public Optional<BinTable> getBinTableByName(String hduExtName) {
        if(!hduNames.containsKey(hduExtName)){
            return Optional.empty();
        }
        HDU hdu = hduNames.get(hduExtName);
        return hdu.getBinTable();
    }

//    public Optional<BinTable> getHeapForBinTable(String hduExtName) {
//        if(!hduNames.containsKey(hduExtName)){
//            return Optional.empty();
//        }
//        HDU hdu = hduNames.get(hduExtName);
//        try {
//            DataInputStream inputStream = getInputStreamForHDUData(hdu);
//            return Optional.of(new (hdu, inputStream));
//        } catch (IOException e) {
//            return Optional.empty();
//        }
//    }
//
//
    /**
     *
     * @param hdu
     * @return
     * @throws IOException
     */
    public DataInputStream getInputStreamForHDUData(HDU hdu) throws IOException {
        DataInputStream stream = getDecompressedDataStream(url);


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

    /**
     * Perform some sanity checks on the bintable.
     *  1. The TFIELDS keyword must exist.
     *  2. The number of columns stored in the TFIELD line has to fit the number
     *     of TTPYE fields
     *  3. Make sure the NAXIS2 keyword exists. It gives the number of rows.
     *  4. NAXIS2 needs to be non-negative.
     *
     * @param hdu the hdu to check
     */
    private void checkSanityForBinTableHDU(HDU hdu) {
        int tfields = hdu.getInt("TFIELDS").orElseThrow(() -> {
            log.error("The TFIELDS keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.2.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing TFIELD keyword");
        });
//
//        if(columns.size() != tfields){
//            log.error("The value of TFIELDS: {} does not match the number of TTYPEn,TBCOLn,TFORMn tuples {}" +
//                    "\nSee section 7.2.1 of the Fits 3.0 standard", tfields, columns.size());
//            throw new IllegalArgumentException("Number of TFIELDS does not match number of TFORMn entries.");
//        }
//

        int naxis1 = hdu.getInt("NAXIS1").orElseThrow(() -> {
            log.error("The NAXIS1 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if(naxis1 < 0){
            throw new IllegalArgumentException("Number of rows (NAXIS1) is negative.");
        }

        int naxis2 = hdu.getInt("NAXIS2").orElseThrow(() -> {
            log.error("The NAXIS2 keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing NAXIS2 keyword");
        });

        if(naxis2 < 0){
            throw new IllegalArgumentException("Number of rows (NAXIS2) is negative.");
        }

        int pcount = hdu.getInt("PCOUNT").orElseThrow(() -> {
            log.error("The PCOUNT keyword cannot be found in the BinTable. " +
                    "Its mandatory.\nSee section 7.3.1 of the Fits 3.0 standard");
            return new IllegalArgumentException("Missing PCOUNT keyword");

        });

        if(pcount < 0){
            throw new IllegalArgumentException("Number of bytes in Heap (PCOUNT) is negative.");
        }

        if (pcount > 0){
            int theap = hdu.getInt("THEAP").orElseThrow(() -> {
                log.error("The THEAP keyword cannot be found in the BinTable. " +
                        "Its mandatory when PCOUNT is larger than 0.\nSee section 7.3.2 of the Fits 3.0 standard");
                return new IllegalArgumentException("Missing PCOUNT keyword");

            });

            if(theap < 0){
                throw new IllegalArgumentException("Number of bytes in to skip to the heap (THEAP) is negative.");
            }

            if(theap != naxis2*naxis2){
                throw new IllegalArgumentException("The value for THEAP must be equal to NAXIS1*NAXIS2." +
                        "\nSee section 7.3.2 of the Fits 3.0 standard");
            }
        }
    }

}
