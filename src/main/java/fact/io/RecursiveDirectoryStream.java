package fact.io;

import stream.Data;
import stream.io.AbstractStream;
import stream.io.SourceURL;
import stream.io.Stream;
import stream.io.multi.AbstractMultiStream;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * Created by mackaiver on 9/21/14.
 */
public class RecursiveDirectoryStream extends AbstractMultiStream {

    static BlockingQueue<File> files = new LinkedBlockingQueue<>();

    private int  maxDepth = 3;
    private String suffix = "Events.fits.gz";
    private AbstractStream stream;
    private boolean needNewFile = true;

    public RecursiveDirectoryStream(SourceURL url) {
        super(url);

    }

    @Override
    public void init() throws Exception {
        System.out.println("called init");
        if(!files.isEmpty()){
            System.out.println("files allready loaded");
            //file allready loaded
            return;
        }
        SourceURL url = getUrl();
        File f = new File(url.getFile());
        if (!f.isDirectory()){
            throw new IllegalArgumentException("Provided url does not point to a directory");
        }

        walkAndAddToQueue(f,0);
        System.out.println("NUBER OF FILE SOMFG: " + files.size());
        //super.init();
    }

    private void walkAndAddToQueue(File dir, int depth){

        if(depth > maxDepth){
            return;
        }
        //get all files ending with the right suffix in this direcotry and add them to the queueeueue
        String[] fileNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                if (f.isHidden() || f.isDirectory()){
                    return false;
                }
                if (name.endsWith(suffix)) {
                    return true;
                }
                return false;

            }
        });
        for(String fName : fileNames){
            files.add(new File(dir, fName));
        }
        //now get all subdirs and do it again
        String[] directoryNames = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File f = new File(dir, name);
                if (!f.isHidden() && f.isDirectory()){
                    return true;
                }
                return false;
            }
        });
        depth++;
        for (String dirName  : directoryNames){
            File subDir = new File(dir, dirName);
            walkAndAddToQueue(subDir, depth);
        }
        return;
    }

    private SourceURL createUrl(String fileName) throws Exception {
        String urlString = url.getProtocol() + ":" + url.getPath() + "/"
                + fileName;
        return new SourceURL(urlString);
    }
    @Override
    public Data readNext() throws Exception {
        if (stream == null) {
            stream = (AbstractStream) streams.get(additionOrder.get(0));
            File f = files.poll();
            if (f == null) {
                return null;
            }
            stream.setUrl(new SourceURL(f.toURI().toURL()));
            stream.init();
        }
        Data data = stream.read();
        if (data != null) {
            return data;
        } else {
            File f = files.poll();
            if (f == null)
                return null;
            else {
                stream.close();
                stream.setUrl(new SourceURL(f.toURI().toURL()));
                stream.init();
                data = stream.read();
                return data;
            }
        }
    }

}
