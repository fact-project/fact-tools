package fact.mapping.ui.components.cameradisplay;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.mapping.FactPixelMapping;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import fact.mapping.ui.components.cameradisplay.colormapping.*;
import fact.mapping.ui.components.selectors.CameraOverlayKeySelector;
import fact.mapping.ui.events.OverlaySelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * This panel contains the hexmap with the overlay selector below it. The colorbar is part of the hexmap.
 *
 * Created by kaibrugge on 02.06.14.
 */
public class DisplayPanel extends JPanel implements EventObserver{

    static Logger log = LoggerFactory.getLogger(DisplayPanel.class);

    final FactHexMapDisplay hexmap = new FactHexMapDisplay(FactPixelMapping.getInstance(), 7, 600, 530);
    final CameraOverlayKeySelector selector = new CameraOverlayKeySelector();
    private final Set<Class<? extends ColorMapping>> colorMapClasses;


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

        //get all classes that implement the colormapping interface
        Reflections reflections = new Reflections("fact");
        colorMapClasses = reflections.getSubTypesOf(ColorMapping.class);

        //setup the hexmap component of the viewer
        hexmap.setBackground(Color.BLACK);


        //--------action listeners for menus and buttons----------

        //actionlistener for colormap context menu.
        ActionListener colorMapMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(Class<? extends ColorMapping> mapClass: colorMapClasses){
                    if(e.getActionCommand().equals(mapClass.getSimpleName())){
                        try {
                            hexmap.setColorMap(mapClass.newInstance());
                        } catch (InstantiationException e1) {
                            log.error("Caught InstantiationException while trying to add new colormap with name: "
                                    + mapClass.getSimpleName()+ ".  Colormaps must have a constructor with zero " +
                                    "parameters (nullary constructor)");
                        } catch (IllegalAccessException e1) {
                            log.error("Caught IllegalAccessException while trying to add new colormap with name: "
                                    + mapClass.getSimpleName()+ ".  Constructor Private?");
                        }
                    }
                }
            }
        };

        // Build a context menu for color mapping and add it to the hexmap
        JPopupMenu popupMenu = new JPopupMenu("Color Mapping");
        for (Class<? extends ColorMapping> map : colorMapClasses){
            JMenuItem colorMapMenuItem1 = new JMenuItem(map.getSimpleName());
            colorMapMenuItem1.addActionListener(colorMapMenuListener);
            popupMenu.add(colorMapMenuItem1);
        }

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
