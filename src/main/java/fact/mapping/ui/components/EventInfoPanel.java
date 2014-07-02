/**
 * 
 */
package fact.mapping.ui.components;

import com.google.common.eventbus.Subscribe;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import fact.mapping.CameraPixel;
import fact.mapping.FactCameraPixel;
import fact.mapping.ui.Bus;
import fact.mapping.ui.EventObserver;
import fact.mapping.ui.PixelSelectionObserver;
import org.apache.commons.math3.util.Pair;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Kai
 * 
 */
public class EventInfoPanel extends JPanel implements EventObserver, PixelSelectionObserver {
    final private String disabledString = "No Data in Event";
	/** The unique class ID */
	private static final long serialVersionUID = -4439223773970111981L;
	JLabel eventNumber = new JLabel("EventNumber: ");
    JTextField roiField = new JTextField(disabledString);
    JTextField timeField = new JTextField(disabledString);
    JTextField runIDField = new JTextField(disabledString);
    DefaultListModel<String> model = new DefaultListModel<>();
    JList<String> pixIDList = new JList<>(model);


    @Override
    @Subscribe
    public void handlePixelSelectionChange(Set<CameraPixel> selectedPixel) {
        model.clear();
        for (CameraPixel sp : selectedPixel){
            FactCameraPixel p = (FactCameraPixel) sp;
            String m = "Chid: " + p.id + "  SoftId: " + p.softid  + "    Coordinates: " + p.getXPositionInMM() + ", " + p.getYPositionInMM();
            model.addElement(m);
        }
    }

    @Override
    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair) {
        Data item = itemKeyPair.getFirst();
        //System.out.println(item.toString());
        Integer roi = (Integer)item.get("NROI");
        if(roi != null){
            roiField.setText(roi.toString());
            roiField.setEnabled(true);
        }
        Serializable eventNum = item.get("EventNum");
        if(eventNum !=  null){
            eventNumber.setText(eventNum.toString());
            eventNumber.setEnabled(true);
        }
        Serializable unixtime = item.get("DATE");
        if(unixtime !=  null){
            timeField.setText(unixtime.toString());
            timeField.setEnabled(true);
        }
        Serializable runid = item.get("RUNID");
        if(runid !=  null){
            runIDField.setText(runid.toString());
            runIDField.setEnabled(true);
        }
    }



    public EventInfoPanel(int width, int height) {

        Bus.eventBus.register(this);

        this.setPreferredSize(new Dimension(width , height));
        JPanel contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(width -12 , height-5));
        //define some padding using an empty border
        roiField.setEditable(false);
        timeField.setEditable(false);
        runIDField.setEditable(false);

        roiField.setEnabled(false);
        runIDField.setEnabled(false);
        timeField.setEnabled(false);

       // overLayPane.setPreferredSize(new Dimension(getWidth(), 150));


        // set layout of the main window
        FormLayout layout = new FormLayout(new ColumnSpec[] {
                ColumnSpec.decode("left:pref:grow"),
                ColumnSpec.decode("pref"),

                ColumnSpec.decode("fill:100px:grow"),
                ColumnSpec.decode("pref"),

                ColumnSpec.decode("right:pref:grow"),
                ColumnSpec.decode("pref"),

        },
                new RowSpec[] {
                        RowSpec.decode("fill:12dlu:grow"),
                        RowSpec.decode("fill:pref:grow"),
                        RowSpec.decode("fill:12dlu:grow"),
                        RowSpec.decode("top:pref"),
                });

        PanelBuilder builder = new PanelBuilder(layout);
        CellConstraints cc = new CellConstraints();
        builder.addSeparator("Event Information", cc.xywh(1,1,6,1));
        builder.add(new JLabel("ROI "), cc.xy(1, 2));
        builder.add(roiField, cc.xy(2, 2));

        builder.add(new JLabel("Time "), cc.xy(3, 2));
        builder.add(timeField, cc.xy(4, 2));

        builder.add(new JLabel("Run ID "), cc.xy(5, 2));
        builder.add(runIDField, cc.xy(6, 2));
        builder.addSeparator("Selection Information", cc.xywh(1,3,6,1));
        builder.add(new JLabel(("Pixel IDs")), cc.xy(1,4));
        builder.add(new JScrollPane(pixIDList), cc.xywh(2,4,5, 1));




        contentPanel.add(builder.getPanel());
        add(contentPanel);

	}


    public static void main(String[]args){
        JFrame frame = new JFrame();
        frame.setSize(400, 300);

        EventInfoPanel n = new EventInfoPanel(400,300);

        frame.getContentPane().add(n);
        frame.pack();
        frame.setVisible(true);
    }



}