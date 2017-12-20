package fact.plotter.ui;

import org.jfree.chart.renderer.category.StatisticalBarRenderer;

import java.awt.*;

/**
 * shamelessly stolen from http://javabeanz.wordpress.com/2007/07/04/creating-barcharts-with-custom-colours-using-jfreechart/
 *
 * @author bruegge
 */
public class CustomStatisticalBarRender extends StatisticalBarRenderer {
    private static final long serialVersionUID = -1781361840471596081L;

    private Paint[] colors;

    public CustomStatisticalBarRender() {
        this.colors = new Paint[]{Color.red, Color.blue, Color.green,
                Color.yellow, Color.orange, Color.cyan,
                Color.magenta, Color.blue};
    }

    public Paint getItemPaint(final int row, final int column) {
        // returns color for each column
        return (this.colors[column % this.colors.length]);
    }

}
