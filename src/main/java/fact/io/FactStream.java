package fact.io;

import java.io.File;

/**
 * Interface defining common methods a fact stream can, but doesnt have to, implement.
 * Created by kaibrugge on 14.04.15.
 */
public interface FactStream {

    /**
     * Set the path to the .drs file that should be used with this stream.
     * The stream can for example add the key @drsPath to the data items.
     * @param drsFile
     */
    public void setDrsFile(File drsFile);

}
