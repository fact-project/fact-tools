/**
 *
 */
package fact.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import stream.Data;
import fact.Constants;
import fact.EventUtils;
import fact.FactViewer;
import fact.viewer.actions.ChangeColorMap;

public class MapView extends JPanel {

	private static final long serialVersionUID = 1L;
	/** The unique class ID */
	JComboBox comboBox;
	short selectIndex = 0;
	private int slice;
	private CameraPixelMap cameraPixelMap;

	public CameraPixelMap getCamMap() {
		return cameraPixelMap;
	}

	public void setCamMap(CameraPixelMap camMap) {
		this.cameraPixelMap = camMap;
	}

	Data event;
	final FactViewer mainWindow;
	private ScalePanel scale;
	boolean live = false;
	boolean sourceSelection = false;
	private String key="Data";

	private int[] showerChids;

	
	public MapView(FactViewer m, boolean live,  boolean sourceSelection, double pixelSize) {
		this.sourceSelection = sourceSelection;
		this.live = live;
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		this.setLayout(new BorderLayout(0, 0));
		cameraPixelMap = new CameraPixelMap(pixelSize);
		cameraPixelMap.setSelectable(false);
		cameraPixelMap.setBackground(Color.BLACK);
		this.add(cameraPixelMap, BorderLayout.CENTER);
		scale = new ScalePanel(cameraPixelMap.getColorMapping(), cameraPixelMap.getMinValue(), cameraPixelMap.getMaxValue());
		scale.setBackground(cameraPixelMap.getBackground());
		this.add(scale, BorderLayout.EAST);
		if(sourceSelection){
			comboBox = new JComboBox();
			this.add(comboBox, BorderLayout.NORTH);
			/**
			 * Add the right Strings to the combox and add actionlisteners
			 *
			 */
			comboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (arg0.getSource() == comboBox && comboBox.getItemCount()!=0 ) {
						String sel = (String) comboBox.getSelectedItem();
						if (sel.endsWith(" Static Map")) {
							sel = (String) sel.subSequence(0, sel.length()
									- " Static Map".length());
						}
//						System.out.println("------------" + sel);
						cameraPixelMap.setData(EventUtils.toDoubleArray(event.get(sel)));
					}
				}
			});
		}
		this.revalidate();

		JPopupMenu popupMenu = new JPopupMenu("Title");
		ChangeColorMap cmAction = new ChangeColorMap(cameraPixelMap, scale);

	    JMenuItem colorMapMenuItem1 = new JMenuItem("TwoToneAbsolute");
	    colorMapMenuItem1.addActionListener(cmAction);
	    popupMenu.add(colorMapMenuItem1);

	    JMenuItem colorMapMenuItem2 = new JMenuItem("NeutralColor");
	    popupMenu.add(colorMapMenuItem2);
	    colorMapMenuItem2.addActionListener(cmAction);

	    JMenuItem colorMapMenuItem3 = new JMenuItem("GrayScale");
	    popupMenu.add(colorMapMenuItem3);
	    colorMapMenuItem3.addActionListener(cmAction);


		cameraPixelMap.setComponentPopupMenu(popupMenu);

	}



	public void setEvent(Data event) {
		this.event = event;
		String currentSelection = null;
		if(sourceSelection){
			currentSelection = (String)comboBox.getSelectedItem();
			comboBox.removeAllItems();
		}

		if(sourceSelection)
		{
			if (event == null) {
				return;
			}
			for (String key : event.keySet()) {
				Serializable val = event.get(key);
				if (val.getClass().isArray()) {
						int length = 0;
						if (val.getClass().getComponentType() == float.class) {
							float[] fVal = (float[]) val;
							length = fVal.length;
						} else if (val.getClass().getComponentType() == double.class) {
							double[] fVal = (double[]) val;
							length = fVal.length;
						} else if (val.getClass().getComponentType() == int.class) {
							int[] fVal = (int[]) val;
							length = fVal.length;
						}
						if (length == Constants.NUMBEROFPIXEL) {
							comboBox.addItem(key + " Static Map");
						} else if (length >= Constants.NUMBEROFPIXEL) {
							comboBox.addItem(key);
						}
				}
			}
			cameraPixelMap.setEvent(event);
			if(currentSelection != null){
				comboBox.setSelectedItem(currentSelection);
			}
		}
		if(live){
			if (event != null) {
				int length = 0;
				Serializable val = event.get(key);
				if (val.getClass().isArray()) {
					if (val.getClass().getComponentType() == double.class) {
						length = ((double[]) (event.get(key))).length;
						if(showerChids != null){
							cameraPixelMap.setData(setIds(showerChids,(double[]) (event.get(key))));
						} else {
							cameraPixelMap.setData((double[]) (event.get(key)));
						}
					}
				}
				for (int i = 0; i < length/Constants.NUMBEROFPIXEL; i++ ){
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.setSlice(i);
				}
			}
		}
		scale.setMax(cameraPixelMap.getMaxValue());
		scale.setMin(cameraPixelMap.getMinValue());
		scale.repaint();
	}
	public double[] setIds(int[] chids, double[] values){
		int roi = values.length/Constants.NUMBEROFPIXEL;
		for (int pix = 0; pix < Constants.NUMBEROFPIXEL; pix++){
			if(!EventUtils.arrayContains(chids, pix)){
				for (int slice = 0; slice < roi; slice++){
					int pos = pix * roi + slice;
							values[pos] = 0.0f;
				}
			}
		}
		return values;
	}


	public void setData(double[] valArray){
		cameraPixelMap.setData(valArray);
		scale.setMax(cameraPixelMap.getMaxValue());
		scale.setMin(cameraPixelMap.getMinValue());
		scale.repaint();
	}
	public void setSlice(int i) {
		slice = i;
		cameraPixelMap.setCurrentSlice(slice);

	}

	public void setActiveOverlayKeys(Set<String> selectedKeys) {
		cameraPixelMap.setActiveOverlayKeys(selectedKeys);
	}
	
	public int[] getShowerChids() {
		return showerChids;
	}
	public void setShowerChids(int[] showerChids) {
		this.showerChids = showerChids;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	
}
