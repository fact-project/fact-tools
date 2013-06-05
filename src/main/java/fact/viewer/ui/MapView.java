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
import fact.FactViewer;
import fact.data.EventUtils;
import fact.viewer.actions.ChangeColorMap;

public class MapView extends JPanel {

	private static final long serialVersionUID = 1L;
	/** The unique class ID */
	JComboBox comboBox;
	short selectIndex = 0;
	private int slice;
	private CameraPixelMap cameraMap;

	public CameraPixelMap getCamMap() {
		return cameraMap;
	}

	public void setCamMap(CameraPixelMap camMap) {
		this.cameraMap = camMap;
	}

	Data event;
	final FactViewer mainWindow;
	private ScalePanel scale;
	boolean live = false;
	boolean sourceSelection = false;
	private String key="Data";
	
	private int[] showerChids;
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

	public MapView(FactViewer m, boolean live, boolean sourceSelection) {
		this.sourceSelection = sourceSelection;
		this.live = live;
		mainWindow = m;
		/**
		 * Add cameraPixelMap, scalePanel and set some Layout stuff:
		 */
		this.setLayout(new BorderLayout(0, 0));
		cameraMap = new CameraPixelMap(5.0d);
		cameraMap.setSelectable(false);
		cameraMap.setBackground(Color.BLACK);
		this.add(cameraMap, BorderLayout.CENTER);
		scale = new ScalePanel(cameraMap.getColorMapping(), cameraMap.getMinValue(), cameraMap.getMaxValue());
		scale.setBackground(cameraMap.getBackground());
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
						cameraMap.setData((float[]) EventUtils.toFloatArray(event.get(sel)));
					}
				}
			});
		}
		this.revalidate();
		
		JPopupMenu popupMenu = new JPopupMenu("Title");
		ChangeColorMap cmAction = new ChangeColorMap(cameraMap, scale);
		
	    JMenuItem colorMapMenuItem1 = new JMenuItem("TwoToneAbsolute");
	    colorMapMenuItem1.addActionListener(cmAction);
	    popupMenu.add(colorMapMenuItem1);

	    JMenuItem colorMapMenuItem2 = new JMenuItem("NeutralColor");
	    popupMenu.add(colorMapMenuItem2);
	    colorMapMenuItem2.addActionListener(cmAction);
	    
	    JMenuItem colorMapMenuItem3 = new JMenuItem("GrayScale");
	    popupMenu.add(colorMapMenuItem3);
	    colorMapMenuItem3.addActionListener(cmAction);
	    
	    
		cameraMap.setComponentPopupMenu(popupMenu);
		
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
			cameraMap.setEvent(event);
			if(currentSelection != null){
				comboBox.setSelectedItem(currentSelection);
			}
		}
		if(live){
			if (event != null) {
				int length = 0;
				Serializable val = event.get(key);
				if (val.getClass().isArray()) {
					if (val.getClass().getComponentType() == float.class) {
						length = ((float[]) (event.get(key))).length;
						if(showerChids != null){
							cameraMap.setData(setIds(showerChids,(float[]) (event.get(key))));
						} else {
							cameraMap.setData((float[]) (event.get(key)));
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
		scale.setMax(cameraMap.getMaxValue());
		scale.setMin(cameraMap.getMinValue());
		scale.repaint();
	}
	public float[] setIds(int[] chids, float[] values){
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


	public void setData(float[] valArray){
		cameraMap.setData(valArray);
		scale.setMax(cameraMap.getMaxValue());
		scale.setMin(cameraMap.getMinValue());
		scale.repaint();
	}
	public void setSlice(int i) {
		slice = i;
		cameraMap.selectSlice(slice);

	}

	public void setActiveOverlayKeys(Set<String> selectedKeys) {
		cameraMap.setActiveOverlayKeys(selectedKeys);
	}

}
