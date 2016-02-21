/**
 *
 */
package fact;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fact.hexmap.ui.Viewer;
import stream.Data;
import stream.ProcessContext;
import stream.StatefulProcessor;
import stream.annotations.Parameter;
import streams.runtime.Hook;
import streams.runtime.Signals;

/**
 * @author chris
 *
 */
public class ShowViewer implements StatefulProcessor {

    static Logger log = LoggerFactory.getLogger(ShowViewer.class);
    Viewer viewer = null;
    AtomicBoolean lock = new AtomicBoolean(true);
    AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * The key for the data to be displayed on the screen
     */
    @Parameter(required = true)
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Parameter(required = false, description = "The default plot range in the main viewer")
    private Integer[] range;

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

        Signals.register(new Hook() {
            @Override
            public void signal(int flags) {
                log.debug("Signal received: '{}', releasing lock", flags);
                closed.set(true);
                synchronized (lock) {
                    lock.set(false);
                    log.debug("set lock to 'false'");
                    lock.notifyAll();
                    log.debug("Notified everyone...");
                }
            }
        });
    }

    /**
     * @see stream.Processor#process(stream.Data)
     */
    @Override
    public Data process(final Data input) {

        if (!input.containsKey(key)) {
            throw new RuntimeException("Key " + key + " not found in event. Cannot show viewer");
        }

        if (closed.get()) {
            log.debug("viewer closed, passing through...");
            return input;
        }

        lock.set(true);

        Thread t = new Thread() {
            public void run() {
                if (viewer == null) {
                    viewer = Viewer.getInstance();
                    viewer.setDefaultKey(key);
                    if (range != null) {
                        viewer.setRange(range);
                    }
                    viewer.getNextButton().setEnabled(true);
                    viewer.getNextButton().addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            synchronized (lock) {
                                lock.set(!lock.get());
                                log.debug("Notifying all listeners on lock...");
                                lock.notifyAll();
                            }
                        }
                    });
                }
                viewer.setVisible(true);
                viewer.setDataItem(input);
            }
        };
        // t.setDaemon(true);
        t.start();

        synchronized (lock) {
            while (lock.get()) {
                try {
                    log.debug("Waiting on lock...");
                    lock.wait();
                    log.debug("Notification occured on lock!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return input;
    }

    @Override
    public void resetState() throws Exception {

    }

    @Override
    public void finish() throws Exception {

    }
}