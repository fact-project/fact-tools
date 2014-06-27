package fact.mapping.ui.windows;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import fact.mapping.ui.components.MainPlotPanel;
import fact.mapping.ui.components.selectors.IntervallMarkerKeySelector;
import fact.mapping.ui.components.selectors.KeySelector;
import fact.mapping.ui.components.selectors.TimeSeriesKeySelector;
import fact.mapping.ui.events.IntervallMarkerSelectionChangedEvent;
import fact.mapping.ui.events.TimeSeriesSelectionChangedEvent;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class PlotDisplayWindow implements EventObserver {

	private final MainPlotPanel plotPanel = new MainPlotPanel(750, 480, false);
    private final KeySelector keySelector = new TimeSeriesKeySelector();
    private final KeySelector intervalKeySelector = new IntervallMarkerKeySelector();

    private Set<Pair<String, Color>> selectedItems = new HashSet<>();
    private Set<Pair<String, Color>> selectedMarker = new HashSet<>();

    public PlotDisplayWindow() {
        //keySelector.addItem(new SeriesKeySelectorItem(key, Color.DARK_GRAY, keySelector));
        Bus.eventBus.register(this);
	}

    public void showWindow(){
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

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        builder.add(plotPanel, cc.xywh(1,1,1,4));
        builder.addSeparator("Series Selection", cc.xy(2, 1));
        builder.add(keySelector, cc.xy(2, 2));
        builder.addSeparator("Marker Selection", cc.xy(2, 3));
        builder.add(intervalKeySelector, cc.xy(2, 4));

        frame.setContentPane(builder.getPanel());
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }


    @Subscribe
    public void handleKeySelectionChange(TimeSeriesSelectionChangedEvent e){
        plotPanel.setItemsToPlot(keySelector.getSelectedItemPairs());
        //plotPanel.setMarkerToPlot(selectedMarker);
        plotPanel.drawPlot();
    }
    @Subscribe
    public void handleKeySelectionChange(IntervallMarkerSelectionChangedEvent e){
        //plotPanel.setItemsToPlot(selectedItems);
        plotPanel.setMarkerToPlot(intervalKeySelector.getSelectedItemPairs());
        plotPanel.drawPlot();
    }

    /**
     * Adds the keys we can display in the plot window to the list on right side of the screen.
     * @param itemKeyPair the current data item we want to display
     */
    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {

        plotPanel.setItemsToPlot(keySelector.getSelectedItemPairs());
        plotPanel.setMarkerToPlot(intervalKeySelector.getSelectedItemPairs());

        plotPanel.drawPlot();
    }



    public static void main(String[]args){
        PlotDisplayWindow p = new PlotDisplayWindow();
        p.showWindow();
    }

}
