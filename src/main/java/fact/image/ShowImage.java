/**
 * 
 */
package fact.image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import stream.AbstractProcessor;
import stream.Data;
import fact.FactViewer;

/**
 * @author chris
 * 
 */
public class ShowImage extends AbstractProcessor {

	static Logger log = LoggerFactory.getLogger(ShowImage.class);
	FactViewer viewer = null;
	AtomicBoolean lock = new AtomicBoolean(true);

	/**
	 * @see stream.Processor#process(stream.Data)
	 */
	@Override
	public Data process(final Data input) {

		lock.set(true);

		Thread t = new Thread() {
			public void run() {
				if (viewer == null) {
					viewer = FactViewer.getInstance();
					viewer.getNextButton().setEnabled(true);
					viewer.getNextButton().addActionListener(
							new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									synchronized (lock) {
										lock.set(!lock.get());
										log.debug("Notifying all listeners on lock...");
										lock.notifyAll();
									}
								}
							});
					viewer.getPrevButton().setEnabled(true);
					viewer.getPrevButton().addActionListener(
							new ActionListener() {
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
				viewer.setEvent(input);
			}
		};
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
}