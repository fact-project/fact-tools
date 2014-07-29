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
import fact.mapping.ui.colormapping.*;
import fact.mapping.ui.components.selectors.CameraOverlayKeySelector;
import fact.mapping.ui.events.OverlaySelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

        //actionlistener for context menu.
        ActionListener contextMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //export to .png
                if(e.getActionCommand().equalsIgnoreCase("Export to .png")){
                    exportPNG();
                    return;
                }
                //select the colormap
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
            colorMapMenuItem1.addActionListener(contextMenuListener);
            popupMenu.add(colorMapMenuItem1);
        }
        //Add the menu item to export the file to .png
        popupMenu.addSeparator();
        JMenuItem exportItem = new JMenuItem("Export to .png");
        exportItem.addActionListener(contextMenuListener);
        popupMenu.add(exportItem);

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

    /**
     *This method shows a jfilechooser to save the current hexmap image as a png file.
     */
    private void exportPNG(){
        //draw stuff again
        BufferedImage bi = new BufferedImage(this.getSize().width, this.getSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        hexmap.paint(g);
        g.dispose();

        //open a file chooser for png files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".png Images", ".png");

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        int ret = chooser.showSaveDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            //if there isn't already the .png extension, add it.
            File f = chooser.getSelectedFile();
            if(!f.getAbsolutePath().endsWith(".png")){
                f = new File(f + ".png");
            }
            //now write the file
            try {
                ImageIO.write(bi, "png", f);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Couldn't write image. Is the path writable?");
            }
        }
    }
}
