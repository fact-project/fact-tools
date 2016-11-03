package fact.io.hdureader;

import com.google.common.base.Splitter;
import com.google.common.io.CountingInputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * FITS files are split into things called HDUs. This is a simple representation of one.
 * For more details on HDUs see http://fits.gsfc.nasa.gov/fits_primer.html
 *
 * Created by mackaiver on 03/11/16.
 */
public class HDU {
    //Maximum length a HDU header can have in blocks of 2880 bytes.
    private final int blockLimit = 100;

    public enum XTENSION {
        IMAGE, BINTABLE, TABLE, NONE
    }

    public Map<String, HDULine> header = new HashMap<>();

    XTENSION extension = XTENSION.NONE;

    boolean isPrimaryHDU = false;

    String comment = "";

    String history = "";

    public int size = 0;


    /**
     * Here is how this works:
     *  1. Read blocks of 2880 bytes and add lines of 80 chars to a list until the END keyword is found.
     *  2. Iterate over the list of lines and parse the strings into a hashmap.
     * @param inputStream The stream of the fits file
     * @throws IOException in case an error occurs when reading the bytes
     */
    public HDU(DataInputStream inputStream) throws IOException {

        List<String> lines = new ArrayList<>();

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

            lines.addAll(blockLines);


            boolean hasEndKeyWord = blockLines
                    .stream()
                    .anyMatch(a -> a.matches("END$"));

            //store how many blocks are in this hdu
            size = blockNumber*2880;

            if(hasEndKeyWord){
                break;
            }
        }



        //get all interesting information
        header = lines.stream()
                 .filter(a -> !a.matches("COMMENT\\s.+|HISTORY\\s.+|END$"))
                 .map(HDULine::fromString)
                 .collect(Collectors.toMap(
                            hduline -> hduline.key,
                            Function.identity()
                         )
                 );

        comment = lines.stream()
                .filter(a -> a.matches("COMMENT.+"))
                .map(a -> a.substring("COMMENT ".length()))
                .collect(Collectors.joining(" \n "));

        history = lines.stream()
                .filter(a -> a.matches("HISTORY.+"))
                .map(a -> a.substring("HISTORY ".length()))
                .collect(Collectors.joining(" \n "));


        isPrimaryHDU = lines.stream()
                .anyMatch(a -> a.matches("SIMPLE\\s+=\\s+T.*"));



        if (header.containsKey("XTENSION")){
            String xtension = header.get("XTENSION").value;
            extension = XTENSION.valueOf(xtension);
        }
    }

    /**
     * Returns whether if there is data attached to this HDU.
     * @return true iff data is attached to this HDU
     */
    public boolean hasData(){
        return sizeOfDataArea() > 0;
    }

    /**
     * According to the FITS standard a header can contain a DATE keyword. This method returns a LocalDateTime
     * if a date can be found in the header.
     *
     * @return date stored in the HDU header
     */
    public Optional<LocalDateTime> date(){
        if (!header.containsKey("DATE")){
            return Optional.empty();
        }

        String dateString = header.get("DATE").value;
        LocalDateTime.parse(dateString);
        return Optional.of(LocalDateTime.parse(dateString));
    }

    public Optional<HDULine> getHeaderLine(String key){
        return Optional.ofNullable(header.get(key));
    }

    public Optional<String> get(String key){
        if(!header.containsKey(key)){
            return Optional.empty();
        }
        return Optional.ofNullable(header.get(key).value);
    }

    /**
     * Get the number of bytes you need to skip in order to jump to the next HDU.
     * From the size of the data block after the header we calculate the offset to
     * either the next hdu or the end of the file. This are multiples of 2880 bytes.
     *
     * @return the number of bytes to skip to hte next hdu.
     */
    public long offsetToNextHDU(){
        if(!hasData()){
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
    public long sizeOfDataArea(){
        long factorNAXIS = header.entrySet().stream()
                .filter(e -> e.getKey().matches("NAXIS\\d+"))
                .mapToLong(e -> e.getValue().getInt().orElse(1))
                .reduce((a, b) -> a * b)
                .orElse(0);

        int bitpix = Math.abs(header.get("BITPIX").getInt().orElse(0));


        //calculate size according to equation 1.
        if(!isPrimaryHDU){
            int gcount = header.get("GCOUNT").getInt().orElse(0);

            int pcount = header.get("PCOUNT").getInt().orElse(0);

            return bitpix * gcount * (pcount + factorNAXIS)/8;
        }

        return bitpix*factorNAXIS/8;
    }


    @Override
    public String toString() {
        return "HDU{" +

                "header=" + header.entrySet().stream()
                .map(e -> "(" + e.getKey() + ", " + e.getValue().value + ")")
                .reduce((a, b) -> a + "\n " + b)
                .orElse(" ") + "\n" +

                "size=" + sizeOfDataArea() + "\n" +
                "date=" + date().toString() +
                '}'
                ;
    }
}
