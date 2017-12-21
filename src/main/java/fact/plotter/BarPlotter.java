package fact.plotter;

import fact.plotter.ui.BarPlotPanel;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;
import stream.ProcessContext;
import stream.annotations.Parameter;
import stream.plotter.DataVisualizer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * This class can plot a bar graph with errorBars by calculating the mean and
 * standarddeviation for a each key and event. The key has to refer to something which can be represented by a Double.
 *
 * @author bruegge
 */
public class BarPlotter extends DataVisualizer {
    static Logger log = LoggerFactory.getLogger(BarPlotter.class);
    private BarPlotPanel histPanel;
    JFrame frame;
    @Parameter(required = false, description = "Flag indicates whether the window stays open after the process has finished", defaultValue = "true")
    public  boolean keepOpen = true;

    @Parameter(required = false, description = "Flag to toggle drawing of Errorbars in plot.")
    public  boolean drawErrors = true;

    @Parameter(required = false, description = "The attributes/features to be plotted")
    public  String[] keys;

    @Parameter(required = false, description = "Title String of the plot", defaultValue = "Default Title")
    public  String title = "Default Title";

    private HashMap<String, SummaryStatistics> summaryStatisticsHashMap = new HashMap<>();


    public BarPlotter() {
        width = 690;
        height = 460;
    }


    @Override
    public void init(ProcessContext ctx) throws Exception {
        super.init(ctx);
        histPanel = new BarPlotPanel(drawErrors, title);
        frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(histPanel, BorderLayout.CENTER);
        frame.setSize(width, height);
        frame.setVisible(true);
        for (String key : keys) {
            summaryStatisticsHashMap.put(key, new SummaryStatistics());
        }
//		frame.setTitle(title);
        if (keys == null) {
            log.error("The keys paramter was null. Did you set it in the .xml file?");
        }
    }

    @Override
    public Data processMatchingData(Data data) {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();

        for (String key : keys) {

            if (data.containsKey(key)) {
                String val = data.get(key).toString();
                double d = Double.parseDouble(val);
                SummaryStatistics ss = summaryStatisticsHashMap.get(key);
                ss.addValue(d);
                dataset.add(ss.getMean(), ss.getStandardDeviation(), " ", key);
            } else {
                log.warn("The key " + key + " does not exist in the Event");
            }
        }
        histPanel.setDataset(dataset);
        histPanel.getPreferredSize();
        return data;
    }

    @Override
    public void finish() throws Exception {
        if (!keepOpen) {
            log.debug("Closing plot frame");
            frame.setVisible(false);
            frame.dispose();
            frame = null;
        } else {
            log.debug("Keeping plot frame visible...");
        }
    }
}
