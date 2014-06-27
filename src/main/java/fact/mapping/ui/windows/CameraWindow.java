package fact.mapping.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.Utils;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import fact.mapping.ui.Viewer;
import fact.mapping.ui.components.cameradisplay.DisplayPanel;
import fact.mapping.ui.components.cameradisplay.FactHexMapDisplay;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This should be able to plot all data in form of a double array which has the same length as the N*numberofpixel.
 *
 * Created by kaibrugge on 13.05.14.
 */
public class CameraWindow implements EventObserver {
    private final DisplayPanel hexMapDisplay;
    private final JComboBox keyComboBox = new JComboBox();
    private Data dataItem;


    public CameraWindow(String key){
        keyComboBox.addItem(key);
        keyComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = (String) ((JComboBox)e.getSource()).getSelectedItem();
                hexMapDisplay.setItemToDisplay(key, dataItem);
                //hexMapDisplay.handleEventChange(Pair.create(dataItem, key));
            }
        });


        hexMapDisplay = new DisplayPanel();
        hexMapDisplay.setBackground(Color.BLACK);

        Bus.eventBus.register(this);
    }

    public void showWindow(){
        JFrame frame = new JFrame();

        // set layout of the main window
        frame.getContentPane().setLayout(
                new FormLayout( new ColumnSpec[]{
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
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        this.dataItem = itemKeyPair.getFirst();
        String selectedKey = (String) keyComboBox.getSelectedItem();
        keyComboBox.removeAllItems();
        for(String key : dataItem.keySet()){
            try{
                double[] data = (double[]) dataItem.get(key);
                if(data.length > 0 && data.length%1440 == 0) {
                    keyComboBox.addItem(key);
                    if(key.equals(selectedKey)){
                        keyComboBox.setSelectedItem(key);
                    }
                }
            } catch(ClassCastException e){
                continue;
            }
        }
    }
}
