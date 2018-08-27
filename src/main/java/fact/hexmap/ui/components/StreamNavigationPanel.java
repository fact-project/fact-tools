/**
 *
 */
package fact.hexmap.ui.components;

import com.google.common.eventbus.Subscribe;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.EventObserver;
import fact.hexmap.ui.events.SliceChangedEvent;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.Serializable;

/**
 * @author chris
 */
public class StreamNavigationPanel extends JPanel implements EventObserver {

    /**
     * The unique class ID
     */
    private static final long serialVersionUID = -4439222773970111981L;
    JTextField fileField = new JTextField(30);
    JLabel eventNumber = new JLabel();
    JLabel triggerTypeField = new JLabel();
    JButton next = new JButton("Next");
    // JButton showTicksButton = new JButton("Show Ticks");
    final JSlider slider = new JSlider();

    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        Data item = itemKeyPair.getFirst();
        Integer roi = (Integer) item.get("ROI");
        if (roi != null) {
            slider.setMaximum(roi - 1);
        }
        Serializable eventNum = item.get("EventNum");
        if (eventNum != null) {
            eventNumber.setText(eventNum.toString());
        }
        Serializable trigger = item.get("TriggerType");
        if (trigger != null) {
            triggerTypeField.setText(trigger.toString());
        }
        Serializable file = item.get("@source");
        if (file != null) {
            fileField.setText(file.toString());
        }
    }

    JTextField sliceField = new JTextField(4);


    public StreamNavigationPanel() {
        super(new FlowLayout(FlowLayout.LEFT));

        Bus.eventBus.register(this);


        add(new JLabel("File:"));
        fileField.setEditable(false);
        add(fileField);

        //add textfield for eventnum below
        add(new JLabel("Event: "));
        add(eventNumber);

        add(new JSeparator(JSeparator.VERTICAL));

        //add textfield for triggertype
        add(new JLabel("Triggertype : "));
        add(triggerTypeField);


        //add nextbutton below
        add(next);

        //add slider  and label for slices below
        slider.setMinimum(0);
        slider.setMaximum(299);
        slider.setValue(0);
        slider.setPreferredSize(new Dimension(400, 35));
        //slider.setPreferredSize(new Dimension(this.getWidth(),
        //		slider.getPreferredSize().height));
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                sliceField.setText(String.valueOf(slider.getValue()));
                Bus.eventBus.post(new SliceChangedEvent(slider.getValue()));
            }
        });

        add(new JLabel("Slice: "));
        add(slider);


        //add textbox for slice number
        sliceField.setEditable(false);
        sliceField.setText("0");
        add(sliceField);
    }


    public JButton getNextButton() {
        return next;
    }

}
