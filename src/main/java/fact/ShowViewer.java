/**
 *
 */
package fact;

import fact.hexmap.ui.Viewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author chris
 */
public class ShowViewer implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(ShowViewer.class);
    Viewer viewer = null;
    AtomicBoolean lock = new AtomicBoolean(true);
    boolean exit = false;

    /**
     * The key for the data to  be displayed on the screen
     */
    @Parameter(required = true)
    public String key;

    @Parameter(required = false, description = "The default plot range in the main viewer")
    public Integer[] range;

    public void setRange(Integer[] range) {
        if (range.length != 2) {
            throw new RuntimeException("The plotrange has to consist of two numbers");
        }
        this.range = range;
    }


    @Override
    public void init(ProcessContext context) throws Exception {
        String os = System.getProperty("os.name");
        log.info("Opening viewer on OS: " + os);

        viewer = Viewer.getInstance();
        viewer.setDefaultKey(key);
        if (range != null) {
            viewer.setRange(range);
        }
        viewer.getNextButton().setEnabled(true);
        viewer.getNextButton().addActionListener((event) -> {
            synchronized (lock) {
                lock.set(!lock.get());
                lock.notifyAll();
            }
        });
        viewer.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                synchronized (lock) {
                    lock.set(!lock.get());
                    lock.notifyAll();
                    exit = true;
                    viewer.dispose();
                }
            }
        });
    }

    /**
     * @see stream.Processor#process(stream.Data)
     */
    @Override
    public Data process(Data item) {
        if (!item.containsKey(key)) {
            throw new RuntimeException("Key " + key + " not found in event. Cannot show viewer");
        }

        viewer.setDataItem(item);
        lock.set(true);
        viewer.setVisible(true);

        synchronized (lock) {
            while (lock.get()) {
                try {
                    lock.wait();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (exit) {
            throw new RuntimeException("ViewerExit");
        }

        return item;
    }


    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}
