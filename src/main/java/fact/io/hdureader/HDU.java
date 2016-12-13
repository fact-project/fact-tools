package fact.io.hdureader;

import com.google.common.base.Splitter;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * FITS files are split into things called HDUs. This is a simple representation of one.
 * For more details on HDUs see http://fits.gsfc.nasa.gov/fits_primer.html
 *
 * Created by mackaiver on 03/11/16.
 */
public class HDU {

//    int headerSizeInBytes = 0;

    public enum XTENSION {
        IMAGE, BINTABLE, TABLE, NONE
    }


    public XTENSION extension = XTENSION.NONE;

    public boolean isPrimaryHDU = false;


    private BinTable binTable = null;


    public final Header header;


    /**
     * Here is how this works:
     *  1. Read blocks of 2880 bytes and add lines of 80 chars to a list until the END keyword is found.
     *  2. Iterate over the list of lines and parse the strings into a hashmap.
     * @param inputStream The stream of the fits file
     * @param url the url to the fits file to read.
     * @param hduOffset the offset the HDU has from the beginning of the file. In bytes.
     * @throws IOException in case an error occurs when reading the bytes
     */
    HDU(DataInputStream inputStream, URL url, long hduOffset) throws IOException {

        int headerSizeInBytes = 0;

        List<String> headerLines = new ArrayList<>();
        int blockLimit = 100;
        for (int blockNumber = 0; blockNumber < blockLimit; blockNumber++) {

            //read a block aka 2880 bytes.
            byte[] headerData = new byte[2880];
            inputStream.readFully(headerData);

            //wrap bytes into a charbuffer and split it into 80 chars each and create a list.
            CharBuffer block = StandardCharsets.US_ASCII.decode(ByteBuffer.wrap(headerData));

            List<String> blockLines = Splitter.fixedLength(80)
                    .omitEmptyStrings()
                    .trimResults()
                    .splitToList(block);

            headerLines.addAll(blockLines);


            boolean hasEndKeyWord = blockLines
                    .stream()
                    .anyMatch(a -> a.matches("END$"));

            //store how many blocks are in this hdu
            headerSizeInBytes = (blockNumber + 1)*2880;

            if(hasEndKeyWord){
                break;
            }
        }


        isPrimaryHDU = headerLines.stream()
                .anyMatch(a -> a.matches("SIMPLE\\s+=\\s+T.*"));

        header = new Header(headerLines, headerSizeInBytes);

        header.get("XTENSION")
                .ifPresent(xtensionValue -> extension = XTENSION.valueOf(xtensionValue));

        if (extension == XTENSION.BINTABLE){
            this.binTable = new BinTable(header, hduOffset, url); //tableName, tableDataStream, heapDataStream);
        }

    }



    public BinTable getBinTable(){
        return this.binTable;
    }


    /**
     * Get the number of bytes you need to skip in order to jump from the end of the last block in the header
     * of this HDU to the beginning of the header block of the next HDU in the file.
     * From the size of the data block after the header we calculate the offset to
     * either the next hdu or the end of the file. This are multiples of 2880 bytes.
     *
     * @return the number of bytes to skip to hte next hdu.
     */
    long offsetToNextHDU(){
        if(sizeOfDataArea() == 0){
            return 0;
        }
        long numberOfBlocks = sizeOfDataArea() / 2880;
        return 2880 * numberOfBlocks + 2880;
    }

    /**
     * Get the size of the data are following the header block.
     * According to equations 1 and 2 in http://fits.gsfc.nasa.gov/standard30/fits_standard30aa.pdf
     * However the size will be returned in bytes and not in bits.
     *
     * @return size of the data area in  bytes.
     */
    long sizeOfDataArea(){
        Map<String, HeaderLine> headerMap = header.headerMap;

        //get NAXIS1*NAXIS2*NAXIS3....
        long factorNAXIS = headerMap.entrySet().stream()
                .filter(e -> e.getKey().matches("NAXIS\\d+"))
                .mapToLong(e -> e.getValue().getInt().orElse(1))
                .reduce((a, b) -> a * b)
                .orElse(0);

        int bitpix = Math.abs(headerMap.get("BITPIX").getInt().orElse(0));


        //calculate size according to equation 1.
        if(!isPrimaryHDU){
            long gcount = headerMap.get("GCOUNT").getLong().orElse(0L);

            long pcount = headerMap.get("PCOUNT").getLong().orElse(0L);

            return bitpix * gcount * (pcount + factorNAXIS)/8L;
        }

        return bitpix*factorNAXIS/8;
    }


    @Override
    public String toString() {
        Map<String, HeaderLine> headerMap = header.headerMap;
        return "HDU{" +
                "header=" + headerMap.entrySet().stream()
                .map(e -> "(" + e.getKey() + ", " + e.getValue().value + ")")
                .reduce((a, b) -> a + "\n " + b)
                .orElse(" ") + "\n" +

                "size=" + sizeOfDataArea() + "\n" +
                "date=" + header.date().toString() + "\n" +
                "extension=" + extension + "\n" +
                "data=" + sizeOfDataArea() +" bytes \n" +
                '}'
                ;
    }
}
