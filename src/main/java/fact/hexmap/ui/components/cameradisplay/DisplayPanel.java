package fact.hexmap.ui.components.cameradisplay;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.hexmap.GifSequenceWriter;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.EventObserver;
import fact.hexmap.ui.colormapping.ColorMapping;
import fact.hexmap.ui.components.selectors.CameraOverlayKeySelector;
import fact.hexmap.ui.events.OverlaySelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * This panel contains the hexmap with the overlay selector below it. The
 * colorbar is part of the hexmap.
 * <p>
 * Created by kaibrugge on 02.06.14.
 */
public class DisplayPanel extends JPanel implements EventObserver {

    static Logger log = LoggerFactory.getLogger(DisplayPanel.class);

    final FactHexMapDisplay hexmap = new FactHexMapDisplay(7, 600, 530);
    final CameraOverlayKeySelector selector = new CameraOverlayKeySelector();
    private final Set<Class<? extends ColorMapping>> colorMapClasses = new HashSet<>();

    public void setItemToDisplay(String key, Data item) {
        hexmap.defaultKey = key;
        hexmap.handleEventChange(Pair.create(item, key));
    }

    /**
     * Adds the keys we can display in the plot window to the list on right side
     * of the screen.
     *
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        hexmap.setOverlayItemsToDisplay(selector.getSelectedItemPairs());
        // hexmap.handleEventChange(itemKeyPair);
    }

    @Subscribe
    public void handleSelectionChange(OverlaySelectionChangedEvent e) {
        hexmap.setOverlayItemsToDisplay(selector.getSelectedItemPairs());
    }

    public DisplayPanel() {
        Bus.eventBus.register(this);

        // get all classes that implement the colormapping interface
        Reflections reflections = new Reflections("fact");
        for (Class<? extends ColorMapping> colorMap : reflections.getSubTypesOf(ColorMapping.class)) {
            if (!Modifier.isAbstract(colorMap.getModifiers())) {
                colorMapClasses.add(colorMap);
            }
        }

        // setup the hexmap component of the viewer
        hexmap.setBackground(Color.BLACK);

        // --------action listeners for menus and buttons----------

        // actionlistener for context menu.
        ActionListener contextMenuListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().equalsIgnoreCase("Patch selection")) {
                    AbstractButton b = (AbstractButton) e.getSource();
                    hexmap.setPatchSelectionMode(b.isSelected());
                }
                // export to .png
                if (e.getActionCommand().equalsIgnoreCase("Export to .png")) {
                    exportPNG();
                    return;
                }

                if (e.getActionCommand().equalsIgnoreCase("Export to .gif")) {
                    exportGIF();
                    return;
                }

                // select the colormap
                for (Class<? extends ColorMapping> mapClass : colorMapClasses) {
                    if (e.getActionCommand().equals(mapClass.getSimpleName())) {
                        try {
                            hexmap.setColorMap(mapClass.newInstance());
                        } catch (InstantiationException e1) {
                            log.error("Caught InstantiationException while trying to add new colormap with name: "
                                    + mapClass.getSimpleName()
                                    + ".  Colormaps must have a constructor with zero "
                                    + "parameters (nullary constructor)");
                        } catch (IllegalAccessException e1) {
                            log.error("Caught IllegalAccessException while trying to add new colormap with name: "
                                    + mapClass.getSimpleName()
                                    + ".  Constructor Private?");
                        }
                    }
                }
            }
        };

        // Build a context menu for color mapping and add it to the hexmap
        JPopupMenu popupMenu = new JPopupMenu("Color Mapping");
        for (Class<? extends ColorMapping> map : colorMapClasses) {
            JMenuItem colorMapMenuItem1 = new JMenuItem(map.getSimpleName());
            colorMapMenuItem1.addActionListener(contextMenuListener);
            popupMenu.add(colorMapMenuItem1);
        }
        // Add the menu item to export the file to .png
        popupMenu.addSeparator();
        JMenuItem exportItem = new JMenuItem("Export to .png");
        exportItem.addActionListener(contextMenuListener);
        popupMenu.add(exportItem);

        JMenuItem exportGIFItem = new JMenuItem("Export to .gif");
        exportGIFItem.addActionListener(contextMenuListener);
        popupMenu.add(exportGIFItem);

        popupMenu.addSeparator();
        JCheckBoxMenuItem patchSelectionMenuItem = new JCheckBoxMenuItem(
                "Patch selection");
        patchSelectionMenuItem.addActionListener(contextMenuListener);
        popupMenu.add(patchSelectionMenuItem);

        hexmap.setComponentPopupMenu(popupMenu);
        selector.setPreferredSize(new Dimension(600, 120));

        // set layout of the main window
        FormLayout layout = new FormLayout(
                new ColumnSpec[]{ColumnSpec.decode("left:pref:grow")},
                new RowSpec[] {
                        RowSpec.decode("fill:530"),
                        RowSpec.decode("center:10dlu:grow"),
                        RowSpec.decode("fill:125")
                }
        );

        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        panel.add(hexmap, cc.xy(1, 1));
        panel.add(new JLabel("Overlays"), cc.xy(1, 2));
        panel.add(selector, cc.xy(1, 3));
        add(panel);
    }

    /**
     * This method shows a jfilechooser to save the current hexmap image as a
     * png file.
     */
    private void exportPNG() {
        // draw stuff again
        BufferedImage bi = new BufferedImage(this.getSize().width,
                this.getSize().height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        hexmap.paint(g, true);
        g.dispose();

        // open a file chooser for png files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".png Images", ".png");

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(filter);
        int ret = chooser.showSaveDialog(null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            // if there isn't already the .png extension, add it.
            File f = chooser.getSelectedFile();
            if (!f.getAbsolutePath().endsWith(".png")) {
                f = new File(f + ".png");
            }
            // now write the file
            try {
                ImageIO.write(bi, "png", f);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Couldn't write image. Is the path writable?");
            }
        }
    }

    public void exportGIF() {
        try {
            ImageOutputStream output = new FileImageOutputStream(new File(
                    "/Volumes/RamDisk/shower.gif"));

            GifSequenceWriter writer = new GifSequenceWriter(output,
                    BufferedImage.TYPE_INT_ARGB, 2, true);

            for (int s = 25; s < 225; s++) {
                try {
                    hexmap.currentSlice = s;
                    System.out.println("Painting slice " + s);
                    BufferedImage bi = new BufferedImage(getSize().width,
                            getSize().height, BufferedImage.TYPE_INT_ARGB);
                    Graphics g = bi.createGraphics();
                    hexmap.paint(g, true);
                    g.dispose();

                    writer.writeToSequence(bi);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            writer.close();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
