package fact.mapping.ui.components.cameradisplay;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.Utils;
import fact.mapping.FactPixelMapping;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import fact.mapping.ui.components.cameradisplay.colormapping.GrayScaleColorMapping;
import fact.mapping.ui.components.cameradisplay.colormapping.NeutralColorMapping;
import fact.mapping.ui.components.cameradisplay.colormapping.RainbowColorMapping;
import fact.mapping.ui.components.cameradisplay.colormapping.TwoToneAbsoluteColorMapping;
import fact.mapping.ui.components.selectors.CameraOverlayKeySelector;
import fact.mapping.ui.events.OverlaySelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This panel contains the hexmap with the overlay selector below it. The colorbar is part of the hexmap.
 *
 * Created by kaibrugge on 02.06.14.
 */
public class DisplayPanel extends JPanel implements EventObserver{
    final FactHexMapDisplay hexmap = new FactHexMapDisplay(FactPixelMapping.getInstance(), 7, 600, 530);
    final CameraOverlayKeySelector selector = new CameraOverlayKeySelector();


    public void setItemToDisplay(String key, Data item){
        hexmap.defaultKey = key;
        hexmap.handleEventChange(Pair.create(item, key));
    }

    /**
     * Adds the keys we can display in the plot window to the list on right side of the screen.
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        hexmap.setOverlayItemsToDisplay(selector.getSelectedItemPairs());
        //hexmap.handleEventChange(itemKeyPair);
    }


    @Subscribe
    public void handleSelectionChange(OverlaySelectionChangedEvent e){
        hexmap.setOverlayItemsToDisplay(selector.getSelectedItemPairs());
    }

    public DisplayPanel(){
        Bus.eventBus.register(this);

        //setup the hexmap component of the viewer
        //hexmap.setDefaultKey(key);
        hexmap.setBackground(Color.BLACK);

        //--------action listeners for menus and buttons----------
        //actionlistener for colormap context menu.
        ActionListener colorMapMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(e.getActionCommand().equals("GrayScale")){

                    hexmap.setColorMap(new GrayScaleColorMapping());

                } else if( e.getActionCommand().equals("NeutralColor")){

                    hexmap.setColorMap(new NeutralColorMapping());

                } else if (e.getActionCommand().equals("TwoToneAbsolute")){

                    hexmap.setColorMap(new TwoToneAbsoluteColorMapping());

                }
            }
        };

        // Build a context menu for color mapping and add it to the hexmap
        JPopupMenu popupMenu = new JPopupMenu("Color Mapping");

        JMenuItem colorMapMenuItem1 = new JMenuItem("TwoToneAbsolute");
        colorMapMenuItem1.addActionListener(colorMapMenuListener);
        popupMenu.add(colorMapMenuItem1);

        JMenuItem colorMapMenuItem2 = new JMenuItem("NeutralColor");
        popupMenu.add(colorMapMenuItem2);
        colorMapMenuItem2.addActionListener(colorMapMenuListener);

        JMenuItem colorMapMenuItem3 = new JMenuItem("GrayScale");
        popupMenu.add(colorMapMenuItem3);
        colorMapMenuItem3.addActionListener(colorMapMenuListener);

        hexmap.setComponentPopupMenu(popupMenu);
        selector.setPreferredSize(new Dimension(600, 120));

        // set layout of the main window
        FormLayout layout = new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("left:pref:grow"),
        },
                new RowSpec[] {
                        RowSpec.decode("fill:530"),
                        RowSpec.decode("center:10dlu:grow"),
                        RowSpec.decode("fill:125"),
                });

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        //first row
        builder.add(hexmap, cc.xy(1, 1));
        builder.addSeparator("Overlays", cc.xy(1, 2));

        builder.add(selector, cc.xy(1, 3));
        //builder.add(overlaySelector, cc.xywh(1,4,6,1));
        add(builder.getPanel());

    }
}
