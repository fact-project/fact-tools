package fact.viewer.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import fact.viewer.colorMappings.ColorMapping;
import fact.viewer.colorMappings.GrayScaleColorMapping;
import fact.viewer.colorMappings.TwoToneAbsoluteColorMapping;
import fact.viewer.ui.CameraPixelMap;
import fact.viewer.ui.NeutralColorMapping;
import fact.viewer.ui.ScalePanel;

public class ChangeColorMap implements ActionListener {

	CameraPixelMap cameraMap;
	ScalePanel scale;
	ColorMapping c;
	public ChangeColorMap(CameraPixelMap cameraMap, ScalePanel scale){
		this.scale = scale;
		this.cameraMap = cameraMap;
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem bla = (JMenuItem) e.getSource();
		float min = cameraMap.minValue;
		float max = cameraMap.maxValue;
		if(bla.getText().equals("TwoToneAbsolute")){
			c = new TwoToneAbsoluteColorMapping();
		} else if(bla.getText().equals("NeutralColor")) {
			c =  new NeutralColorMapping();
		} else if(bla.getText().equals("GrayScale")){
			c = new GrayScaleColorMapping();
		}
		c.setMinMax(min, max);
		cameraMap.setColorMapping(c);
		scale.setColorMapping(c);		
	}

}
