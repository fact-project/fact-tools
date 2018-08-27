package fact.hexmap.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.Constants;
import fact.Utils;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.EventObserver;
import fact.hexmap.ui.components.cameradisplay.DisplayPanel;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This should be able to plot all data in form of a double array which has the same length as the N*1440.
 * <p>
 * Created by kaibrugge on 13.05.14.
 */
public class CameraWindow implements EventObserver {
    private final DisplayPanel hexMapDisplay;
    private final JComboBox<String> keyComboBox = new JComboBox<>();
    private Data dataItem;


    /**
     * The window takes a key to some entry in the Data item which it will display
     *
     * @param key
     */
    public CameraWindow(String key) {
        keyComboBox.addItem(key);
        keyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) ((JComboBox) e.getSource()).getSelectedItem();
                if (key != null) {
                    hexMapDisplay.setItemToDisplay(key, dataItem);
                }
            }
        });


        hexMapDisplay = new DisplayPanel();
        hexMapDisplay.setBackground(Color.BLACK);

        Bus.eventBus.register(this);
    }

    /**
     * Define the layout for the window
     */
    public void showWindow() {
        JFrame frame = new JFrame();

        // set layout of the main window
        frame.getContentPane().setLayout(
                new FormLayout(new ColumnSpec[]{
                        ColumnSpec.decode("right:max(500;pref):grow"),},
                        new RowSpec[]{
                                RowSpec.decode("pref"),
                                RowSpec.decode("default"),
                        }
                )
        );
        frame.getContentPane().add(keyComboBox, "1,1,left,top");
        frame.getContentPane().add(hexMapDisplay, "1,2,left,top");
        frame.getContentPane().setBackground(Color.BLACK);

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    /**
     * Adds the keys we can display in the camera window to the dropdown list.
     *
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        this.dataItem = itemKeyPair.getFirst();
        String selectedKey = (String) keyComboBox.getSelectedItem();
        keyComboBox.removeAllItems();
        for (String key : dataItem.keySet()) {
            double[] data = Utils.toDoubleArray(dataItem.get(key));
            if (data != null && data.length > 0 && data.length % Constants.N_PIXELS == 0) {
                keyComboBox.addItem(key);
                if (key.equals(selectedKey)) {
                    keyComboBox.setSelectedItem(key);
                }
            }
        }
    }
}
