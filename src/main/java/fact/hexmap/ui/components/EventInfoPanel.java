/**
 *
 */
package fact.hexmap.ui.components;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.hexmap.CameraPixel;
import fact.hexmap.ui.Bus;
import fact.hexmap.ui.EventObserver;
import fact.hexmap.ui.PixelSelectionObserver;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Part of the viewer main window.
 * This shows some general information about the currently analyzed file and some more specific
 * information for all the selected pixels. So far the pixel information is a bit crude.
 *
 * @author Kai
 */
public class EventInfoPanel extends JPanel implements EventObserver, PixelSelectionObserver {
    final private String disabledString = "No Data in Event";
    /**
     * The unique class ID
     */
    private static final long serialVersionUID = -4439223773970111981L;
    JLabel eventNumber = new JLabel("EventNumber: ");
    JTextField roiField = new JTextField(disabledString);
    JTextField timeField = new JTextField(disabledString);
    JTextField runIDField = new JTextField(disabledString);
    JTextField chargeField = new JTextField(disabledString);
    JTextField chargeFieldStd = new JTextField(disabledString);
    JTextField sizeField = new JTextField(disabledString);

    JTextField widthField = new JTextField(disabledString);
    JTextField lengthField = new JTextField(disabledString);


    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> pixIDList = new JList<>(model);
    private DescriptiveStatistics eventStatistics;
    private Set<CameraPixel> selectedPixel = new HashSet<>();
    private double[] photonChargeArray;


    @Override
    @Subscribe
    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel) {
        this.selectedPixel = selectedPixel;
        updateSelectionInfo(selectedPixel);
    }

    private void updateSelectionInfo(Set<CameraPixel> selectedPixel) {
        model.clear();
        for (CameraPixel sp : selectedPixel) {
            CameraPixel p = (CameraPixel) sp;
            String m = "Chid: " + p.id + "  SoftId: " + p.softid;
            if (photonChargeArray != null) {
                m += "   photonChqarge: " + photonChargeArray[p.id];
            }

//            m +=  " x in MM " +  ((CameraPixel) sp).getXPositionInMM();
//            m += "  y in MM " + ((CameraPixel) sp).getYPositionInMM();
            model.addElement(m);
        }
    }

    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {

        //get the current event
        Data item = itemKeyPair.getFirst();

        //set the fields if theres data in the event
        Integer roi = (Integer) item.get("NROI");
        if (roi != null) {
            roiField.setText(roi.toString());
            roiField.setEnabled(true);
        }
        Serializable eventNum = item.get("EventNum");
        if (eventNum != null) {
            eventNumber.setText(eventNum.toString());
            eventNumber.setEnabled(true);
        }
        Serializable unixtime = item.get("DATE");
        if (unixtime != null) {
            timeField.setText(unixtime.toString());
            timeField.setEnabled(true);
        }
        Serializable runid = item.get("RUNID");
        if (runid != null) {
            runIDField.setText(runid.toString());
            runIDField.setEnabled(true);
        }
        //get photoncharge if its in the map
        Serializable chargeArray = item.get("@photoncharge");
        if (chargeArray != null) {
            try {
                photonChargeArray = ((double[]) chargeArray);
                eventStatistics = new DescriptiveStatistics();
                for (double v : photonChargeArray) {
                    eventStatistics.addValue(v);
                }
                chargeField.setText("" + eventStatistics.getMean());
                chargeField.setEnabled(true);

                chargeFieldStd.setText("" + eventStatistics.getStandardDeviation());
                chargeFieldStd.setEnabled(true);
            } catch (ClassCastException e) {
                //pass
            }
        }

        //get size if its in the map
        if (item.get("@size") != null) {
            try {
                Double size = (Double) item.get("@size");
                sizeField.setText("" + size);
                sizeField.setEnabled(true);
            } catch (ClassCastException e) {
                //pass
            }
        } else {
            sizeField.setText(disabledString);
            sizeField.setEnabled(false);
        }

        //get width and length if they are in the map
        if (item.get("@width") != null && item.get("@length") != null) {
            try {
                Double width = (Double) item.get("@width");
                Double length = (Double) item.get("@length");
                widthField.setText("" + width);
                widthField.setEnabled(true);

                lengthField.setText("" + length);
                lengthField.setEnabled(true);
            } catch (ClassCastException e) {
                //pass
            }
        } else {
            widthField.setText(disabledString);
            widthField.setEnabled(false);

            lengthField.setText(disabledString);
            lengthField.setEnabled(false);
        }

        if (runid != null) {
            runIDField.setText(runid.toString());
            runIDField.setEnabled(true);
        }

        //update the selection info now that we have the photoncharge
        updateSelectionInfo(this.selectedPixel);
    }


    public EventInfoPanel(int width, int height) {

        Bus.eventBus.register(this);

        this.setPreferredSize(new Dimension(width, height));
        JPanel contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(width - 12, height - 5));
        //define some padding using an empty border
        roiField.setEditable(false);
        timeField.setEditable(false);
        runIDField.setEditable(false);
        chargeField.setEditable(false);
        chargeFieldStd.setEditable(false);
        sizeField.setEditable(false);
        widthField.setEditable(false);
        lengthField.setEditable(false);

        roiField.setEnabled(false);
        runIDField.setEnabled(false);
        timeField.setEnabled(false);
        chargeField.setEnabled(false);
        chargeFieldStd.setEnabled(false);
        sizeField.setEnabled(false);
        widthField.setEnabled(false);
        lengthField.setEnabled(false);
        // set layout of the main window
        FormLayout layout = new FormLayout(new ColumnSpec[]{
                ColumnSpec.decode("left:100px:grow"),
                ColumnSpec.decode("pref"),

                ColumnSpec.decode("fill:100px:grow"),
                ColumnSpec.decode("pref"),

                ColumnSpec.decode("right:pref:grow"),
                ColumnSpec.decode("pref"),
        },
                new RowSpec[]{
                        RowSpec.decode("fill:12dlu:grow"),//roi and stuff
                        RowSpec.decode("fill:pref:grow"),
                        RowSpec.decode("fill:12dlu:grow"),//selection info
                        RowSpec.decode("top:pref"),
                        RowSpec.decode("fill:12dlu:grow"), //physics
                        RowSpec.decode("fill:pref"),
                        RowSpec.decode("fill:pref"),
                        RowSpec.decode("fill:pref")
                });

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Event Information", cc.xywh(1, 1, 6, 1));
        builder.add(new JLabel("ROI "), cc.xy(1, 2));
        builder.add(roiField, cc.xy(2, 2));

        builder.add(new JLabel("Time "), cc.xy(3, 2));
        builder.add(timeField, cc.xy(4, 2));

        builder.add(new JLabel("Run ID "), cc.xy(5, 2));
        builder.add(runIDField, cc.xy(6, 2));
        builder.addSeparator("Selection Information", cc.xywh(1, 3, 6, 1));
        builder.add(new JLabel(("Pixel IDs")), cc.xy(1, 4));
        builder.add(new JScrollPane(pixIDList), cc.xywh(2, 4, 5, 1));

        builder.addSeparator("Addition Physics Information", cc.xywh(1, 5, 6, 1));
        builder.add(new JLabel("Charge Mean/Deviation"), cc.xywh(1, 6, 2, 1));
        builder.add(chargeField, cc.xywh(3, 6, 1, 1));
        builder.add(chargeFieldStd, cc.xywh(4, 6, 1, 1));
        builder.add(new JLabel("Width, Length"), cc.xywh(1, 7, 2, 1));
        builder.add(widthField, cc.xywh(3, 7, 1, 1));
        builder.add(lengthField, cc.xywh(4, 7, 1, 1));

        builder.add(new JLabel("Size (of Shower)"), cc.xywh(1, 8, 2, 1));
        builder.add(sizeField, cc.xywh(3, 8, 2, 1));


        contentPanel.add(builder.getPanel());
        add(contentPanel);
    }


    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(600, 400);

        EventInfoPanel n = new EventInfoPanel(600, 400);

        frame.getContentPane().add(n);
        frame.pack();
        frame.setVisible(true);
    }


}
