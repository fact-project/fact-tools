package fact.hexmap.ui;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.hexmap.ui.components.EventInfoPanel;
import fact.hexmap.ui.components.MainPlotPanel;
import fact.hexmap.ui.components.StreamNavigationPanel;
import fact.hexmap.ui.components.cameradisplay.DisplayPanel;
import fact.hexmap.ui.windows.CameraWindow;
import fact.hexmap.ui.windows.PlotDisplayWindow;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Here the components of the gui are layed out. like the position of the camera map or the chart display in the window.
 * An EventBus is used to communicate with some of the components. In case a new data item from the stream
 * is set  a new Event will be send out and every subscriber (for example the mapDisplay) can react to it.
 * See the setDataItem(Data item)
 *
 * @author kai
 */
public class Viewer extends JFrame {

    /**
     * The unique class ID
     */
    private static final long serialVersionUID = -5687227971590846044L;
    static Logger log = LoggerFactory.getLogger(Viewer.class);


    //------some components for the viewer
    final DisplayPanel mapDisplay = new DisplayPanel();
    final StreamNavigationPanel navigation = new StreamNavigationPanel();
    final MainPlotPanel chartPanel = new MainPlotPanel(550, 350, true);
    final EventInfoPanel eventInfoPanel = new EventInfoPanel(600, 320);


    private String defaultKey;

    public void setDefaultKey(String key) {
        //set a default item to the mainplotpanel
        defaultKey = key;
        chartPanel.setDefaultEntry(defaultKey, Color.red);
    }

    //set plotrange in the plotpanel
    public void setRange(Integer[] range) {
        chartPanel.setRange(range[0], range[1]);
    }

    private Data item;


    //there should also be only 1 instance of the viewer.
    private static Viewer viewer = null;

    public static Viewer getInstance() {
        if (viewer == null) {
            viewer = new Viewer();
        }
        return viewer;
    }

    //the constructor. build layout here. bitch
    private Viewer() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Fact Tools GUI Development");


        //------- add a chart window
        //chartPanel.setBackground(Color.WHITE);
        //chartPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null,
        //        null, null, null));
        JMenuBar menu = createMenuBar();
        this.setJMenuBar(menu);

        // set layout of the main window
        FormLayout layout = new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("fill:605px"),
                ColumnSpec.decode("fill:605px"),},
                new RowSpec[]{
                        RowSpec.decode("fill:pref"),
                        RowSpec.decode("fill:pref"),
                        RowSpec.decode("fill:pref")
                });

        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        panel.add(chartPanel, cc.xywh(2, 1, 1, 1));
        panel.add(eventInfoPanel, cc.xywh(2, 2, 1, 1));
        panel.add(mapDisplay, cc.xywh(1, 1, 1, 2));
        panel.add(navigation, cc.xywh(1, 3, 2, 1));

        setContentPane(panel);

        //setSize(1200, 850);
        pack();
    }


    /**
     * Creates the menu bar and returns it. All menubar setup shoudl happen in here
     *
     * @return
     */
    private JMenuBar createMenuBar() {
        //---add a menu bar on top
        //in case of mac os use the system native menu bar.
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        JMenuBar menu = new JMenuBar();

        JMenu file = new JMenu("File");
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(quit);

        //----- WINDOWS---
        JMenu windows = new JMenu("Windows");
        JMenuItem camWindowMenuItem = new JMenuItem("New Camera Window");
        camWindowMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CameraWindow mw = new CameraWindow(defaultKey);
                Bus.eventBus.post(Pair.create(item, defaultKey));
                mw.showWindow();
            }
        });
        JMenuItem plotWindowItem = new JMenuItem("New Plot Window");
        plotWindowItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PlotDisplayWindow plotDisplay = new PlotDisplayWindow();
                Bus.eventBus.post(Pair.create(item, defaultKey));
                plotDisplay.showWindow();
            }
        });

        windows.add(plotWindowItem);
        windows.add(camWindowMenuItem);

        //------- HELP--------
        JMenu help = new JMenu("Help");
        JMenuItem visitWeb = new JMenuItem("Visit FactTools Website");
        visitWeb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUrl("http://sfb876.tu-dortmund.de/FACT/");
            }
        });
        JMenuItem visitStream = new JMenuItem("Visit StreamsFramework Website");
        visitStream.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUrl("http://www.jwall.org/streams/");
            }
        });
        help.add(visitStream);
        help.add(visitWeb);


        menu.add(file);
        menu.add(windows);
        menu.add(help);
        menu.setBackground(Color.GRAY);
        return menu;
    }

    /**
     * Open the default web browser of the system with the specified url
     * stolen from http://stackoverflow.com/questions/10967451/open-a-link-in-browser-with-java-button
     *
     * @param url the url to open
     */
    public void openUrl(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                if (desktop != null && desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    java.net.URI uri = new java.net.URI(url);
                    desktop.browse(uri);
                }
            }
        } catch (IOException e) {
            log.error("Couldnt connect to desktop environment. Cannot open browser");
        } catch (URISyntaxException e) {
            log.error("Wrong syntax for an url provided by string: " + url);
        }

    }


    public JButton getNextButton() {
        return navigation.getNextButton();
    }


    /**
     * The current data item to be displayed. This will sent an Event to all eventbus subscribers who care about new
     * events from the stream
     *
     * @param item the new item from the stream
     */
    public void setDataItem(Data item) {
        this.item = item;
        Bus.eventBus.post(Pair.create(item, defaultKey));
    }

}
