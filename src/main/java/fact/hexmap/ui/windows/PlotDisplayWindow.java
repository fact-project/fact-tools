package fact.hexmap.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.EventObserver;
import fact.hexmap.ui.components.MainPlotPanel;
import fact.hexmap.ui.components.selectors.IntervallMarkerKeySelector;
import fact.hexmap.ui.components.selectors.KeySelector;
import fact.hexmap.ui.components.selectors.TimeSeriesKeySelector;
import fact.hexmap.ui.events.IntervallMarkerSelectionChangedEvent;
import fact.hexmap.ui.events.TimeSeriesSelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;

public class PlotDisplayWindow implements EventObserver {

    private final MainPlotPanel plotPanel = new MainPlotPanel(750, 480, false);
    private final KeySelector keySelector = new TimeSeriesKeySelector();
    private final KeySelector intervalKeySelector = new IntervallMarkerKeySelector();

//    private Set<Pair<String, Color>> selectedItems = new HashSet<>();
//    private Set<Pair<String, Color>> selectedMarker = new HashSet<>();

    public PlotDisplayWindow() {
        //keySelector.addItem(new SeriesKeySelectorItem(key, Color.DARK_GRAY, keySelector));
        Bus.eventBus.register(this);
    }

    public void showWindow() {
        JFrame frame = new JFrame();
        FormLayout layout = new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("pref"),
                ColumnSpec.decode("pref")
        },
                new RowSpec[]{
                        RowSpec.decode("pref"),
                        RowSpec.decode("240px"),
                        RowSpec.decode("pref"),
                        RowSpec.decode("240px")
                }
        );


        JPanel panel = new JPanel(layout);
        CellConstraints cc = new CellConstraints();
        panel.add(plotPanel, cc.xywh(1, 1, 1, 4));

        panel.add(new JLabel("Series Selection"), cc.xy(2, 1));
        panel.add(keySelector, cc.xy(2, 2));

        panel.add(new JLabel("Marker Selection"), cc.xy(2, 3));
        panel.add(intervalKeySelector, cc.xy(2, 4));

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }


    @Subscribe
    public void handleKeySelectionChange(TimeSeriesSelectionChangedEvent e) {
        plotPanel.setItemsToPlot(keySelector.getSelectedItemPairs());
        //plotPanel.setMarkerToPlot(selectedMarker);
        plotPanel.drawPlot();
    }

    @Subscribe
    public void handleKeySelectionChange(IntervallMarkerSelectionChangedEvent e) {
        //plotPanel.setItemsToPlot(selectedItems);
        plotPanel.setMarkerToPlot(intervalKeySelector.getSelectedItemPairs());
        plotPanel.drawPlot();
    }

    /**
     * Adds the keys we can display in the plot window to the list on right side of the screen.
     *
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {

        plotPanel.setItemsToPlot(keySelector.getSelectedItemPairs());
        plotPanel.setMarkerToPlot(intervalKeySelector.getSelectedItemPairs());

        plotPanel.drawPlot();
    }


    public static void main(String[] args) {
        PlotDisplayWindow p = new PlotDisplayWindow();
        p.showWindow();
    }

}
