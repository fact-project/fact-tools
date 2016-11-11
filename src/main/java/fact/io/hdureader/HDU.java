package fact.io.hdureader;

import com.google.common.base.Splitter;

import java.io.*;
import java.net.URL;
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
    //Maximum length the header of this HDU can have in blocks of 2880 bytes.
    private final int blockLimit = 100;

    public enum XTENSION {
        IMAGE, BINTABLE, TABLE, NONE
    }

    public Map<String, HDULine> header = new HashMap<>();

    XTENSION extension = XTENSION.NONE;

    boolean isPrimaryHDU = false;

    String comment = "";

    String history = "";

    //size of header in bytes
    public int headerSizeInBytes = 0;

    public final long hduOffset;

    private BinTable binTable = null;


    /**
     * Here is how this works:
     *  1. Read blocks of 2880 bytes and add lines of 80 chars to a list until the END keyword is found.
     *  2. Iterate over the list of lines and parse the strings into a hashmap.
     * @param inputStream The stream of the fits file
     * @param url
     * @param hduOffset the offset the HDU has from the beginning of the file. In bytes.  @throws IOException in case an error occurs when reading the bytes
     */
    protected HDU(DataInputStream inputStream, URL url, long hduOffset) throws IOException {

        this.hduOffset = hduOffset;

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
            headerSizeInBytes = (blockNumber + 1)*2880;

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


        if (extension == XTENSION.BINTABLE){

            DataInputStream tableDataStream = Fits.getDecompressedDataStream(url);
            long skippedBytes = tableDataStream.skipBytes((int) (hduOffset + headerSizeInBytes));
            if(skippedBytes != hduOffset + headerSizeInBytes){
                throw new IOException("Could not skip all bytes to data area in this HDU.");
            }



            Integer naxis1 = header.get("NAXIS1").getInt().orElse(0);
            Integer naxis2 = header.get("NAXIS2").getInt().orElse(0);
            Integer theap  = header.get("THEAP").getInt().orElse(naxis1 * naxis2);

            int bytesToSkip = (int) (hduOffset + headerSizeInBytes + theap);

            DataInputStream heapDataStream = Fits.getDecompressedDataStream(url);
            skippedBytes = heapDataStream.skipBytes(bytesToSkip);

            if(skippedBytes != bytesToSkip){
                throw new IOException("Could not skip all bytes to data area in this HDU.");
            }

            String tableName = header.get("EXTNAME").value;

            this.binTable = new BinTable(header, tableName, tableDataStream, heapDataStream);
        }

    }



    public Optional<BinTable> getBinTable(){
        return Optional.of(this.binTable);
    }


//    /**
//     * Returns whether if there is data attached to this HDU.
//     * @return true iff data is attached to this HDU
//     */
//    public boolean hasData(){
//        return sizeOfDataArea() > 0;
//    }

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

//    public Optional<HDULine> getHeaderLine(String key){
//        return Optional.ofNullable(header.get(key));
//    }

    public Optional<Integer> getInt(String key){
        try {
            HDULine line = header.get(key);
            return Optional.of(Integer.parseInt(line.value));
        } catch (NumberFormatException| NullPointerException e){
            return Optional.empty();
        }
    }
    public Optional<String> get(String key){
        if(!header.containsKey(key)){
            return Optional.empty();
        }
        return Optional.ofNullable(header.get(key).value);
    }

    /**
     * Get the number of bytes you need to skip in order to jump from the end of the last block in the header
     * of this HDU to the beginning of the header block of the next HDU in the file.
     * From the size of the data block after the header we calculate the offset to
     * either the next hdu or the end of the file. This are multiples of 2880 bytes.
     *
     * @return the number of bytes to skip to hte next hdu.
     */
    public long offsetToNextHDU(){
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
