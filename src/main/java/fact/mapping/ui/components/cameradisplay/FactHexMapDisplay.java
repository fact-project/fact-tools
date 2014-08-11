/**
 * 
 */
package fact.mapping.ui.components.cameradisplay;

import com.google.common.eventbus.Subscribe;
import fact.Utils;
import fact.mapping.CameraPixel;
import fact.mapping.FactPixelMapping;
import fact.mapping.PixelMapping;
import fact.mapping.ui.*;
import fact.mapping.ui.colormapping.ColorMapping;
import fact.mapping.ui.colormapping.GrayScaleColorMapping;
import fact.mapping.ui.events.SliceChangedEvent;
import fact.mapping.ui.overlays.CameraMapOverlay;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stream.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This implements a PixelMap to draw a grid of hexagons as seen in the camera of the fact telescope
 * The hexagons are equally spaced and sized. Orientated with one edge on the bottom. Also has a colorbar next to it.
 *
 */
public class FactHexMapDisplay extends JPanel implements PixelMapDisplay, SliceObserver, MouseListener {
	/** The unique class ID */
	private static final long serialVersionUID = -4015808725138908874L;

	static Logger log = LoggerFactory.getLogger(FactHexMapDisplay.class);

	FactHexTile tiles[];

	// initialize the hexagonal grid.
    int canvasWidth;
    int canvasHeight;

    int rows = 0, cols = 0;


	Set<CameraPixel> selectedPixels = new LinkedHashSet<CameraPixel>();

	public double[][] sliceValues = new double[1440][1024];
	int currentSlice = 0;

    //the data and key which to display a a hexmap
    private Data dataItem;

    // store the smallest and largest value in the data. We need this to map vales to colors in the display
    private double minValueInData;
    private double maxValueInData;

    //the colormap of this display and the scale next to the map
    private ColorMapping colormap = new GrayScaleColorMapping();

    final private FactPixelMapping pixelMapping;
    private ArrayList<CameraMapOverlay> overlays = new ArrayList<>();
    private Set<Pair<String, Color>> overlayKeys = new HashSet<>();

    //formater to display doubles nicely
    DecimalFormat fmt = new DecimalFormat("#.##");

    //a default key
    public String defaultKey;
    /**
     * A Hexagon in this case is defined by the passed radius. The radius of the circle that fits into the hexagon
     * can be calculated by sqrt(3)/2 * (outter radius)
     * @param radius the radius of the circle the hexagon should fit into
     */
	public FactHexMapDisplay(double radius, int canvasWidth, int canvasHeight) {

        Bus.eventBus.register(this);

        this.pixelMapping = FactPixelMapping.getInstance();
        this.canvasHeight = canvasHeight;
        this.canvasWidth = canvasWidth;
        this.rows = pixelMapping.getNumberRows();
        this.cols = pixelMapping.getNumberCols();

		tiles = new FactHexTile[pixelMapping.getNumberOfPixel()];
        for (int i = 0; i < tiles.length; i++){
            FactHexTile t = new FactHexTile(pixelMapping.getPixelFromId(i), radius);
            tiles[i] = t;
        }

        //add the mosuelistener so we can react to mouse clicks on the hexmap
        this.addMouseListener(this);
	}
    @Override
    public Tile[] getTiles() {
        return tiles;
    }

    @Override
    public PixelMapping getPixelMapping() {
        return pixelMapping;
    }


    @Override
    @Subscribe
    public void handleSliceChangeEvent(SliceChangedEvent ev) {
        //log.debug("Hexmap Selecting slice: {}", ev.currentSlice);
        this.currentSlice = ev.currentSlice;
        this.repaint();
    }

    @Subscribe
    public void handleEventChange(Pair<Data, String> itemKeyPair){
        log.debug("hexmap got a new item");

        String key;
        if (defaultKey != null){
            key = defaultKey;
        } else {
            key = itemKeyPair.getSecond();
        }

        this.dataItem = itemKeyPair.getFirst();
        minValueInData = 0;
        maxValueInData = 0;
        overlays = updateOverlays(overlayKeys, dataItem);
        if (dataItem.containsKey(key)) {
            updateMapDisplay(dataItem, key);
        } else {
            log.error("The key: " + key + " was not found in the data item. Nothing to display.");
        }

    }


    public void setOverlayItemsToDisplay(Set<Pair<String, Color>> items){
        overlayKeys = items;
        overlays = updateOverlays(items, dataItem);
        this.repaint();
    }

    private ArrayList<CameraMapOverlay> updateOverlays(Set<Pair<String, Color>> items, Data dataItem) {
        ArrayList<CameraMapOverlay> overlays = new ArrayList<>();
        for (Pair<String, Color> s : items) {
            CameraMapOverlay overlay = (CameraMapOverlay) dataItem.get(s.getKey());
            if (overlay != null) {
                overlay.setColor(s.getValue());
                overlays.add(overlay);
            }
        }
        return overlays;
    }

    private void updateMapDisplay(Data item, String key) {
        if (item == null){
            log.error("Dataitem was null in cameraWindow");
        }
        try {
            double[] data = (double[]) item.get(key);
            this.sliceValues = Utils.sortPixels(data, 1440);
            for (double[] slices : sliceValues){
                for(double v : slices){
                    minValueInData = Math.min(minValueInData, v);
                    maxValueInData = Math.max(maxValueInData, v);
                }
            }
            this.repaint();
        } catch(ClassCastException e){
            log.error("The viewer can only display data of type double[]");
        }
    }

    @Override
    public void setColorMap(ColorMapping m) {
        this.colormap = m;
        this.repaint();
    }


    //draws a grid using g2d
    private void drawGrid(Graphics2D g2, int step){
        for(int i = 0; i < this.getHeight(); i +=step){
            g2.drawString(Integer.toString(i), 0, i);
            g2.draw(new Line2D.Double(0, i, this.getWidth(), i));
        }
    }

	// The paint method.
	public void paint(Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        int xOffset = getWidth()/2;
        int yOffset = getHeight()/2;
        if(g instanceof Graphics2D)
        {
            Graphics2D g2 = (Graphics2D)g;
           // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
             //       RenderingHints.VALUE_ANTIALIAS_ON);


            //draw a grid with lines every 25 pixel in a dark grey color
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(Color.DARK_GRAY);
            drawGrid(g2, 25);

            //now draw the actual camera pixel
            //translate to center of canvas
            g2.translate(xOffset, yOffset);
            //rotate 90 degrees counter clockwise
            g2.rotate(-Math.PI/2);
            //and draw tiles
            for (Tile tile : tiles) {
                CameraPixel p = tile.getCameraPixel();
                int slice = currentSlice;
                if(currentSlice >= sliceValues[tile.getCameraPixel().id].length){
                    slice = sliceValues[tile.getCameraPixel().id].length - 1;
                }
                double value = sliceValues[tile.getCameraPixel().id][slice];
                tile.setFillColor(this.colormap.getColorFromValue(value, minValueInData, maxValueInData));
                if(selectedPixels.contains(p)){
                    tile.setBorderColor(Color.RED);
                } else {

                    tile.setBorderColor(Color.BLACK);
                }
                tile.paint(g);
            }

            //draw all overlays
            for(CameraMapOverlay o:overlays){
                o.paint(g2, this);
            }
            g2.setStroke(new BasicStroke(1.0f));
            g2.setColor(Color.WHITE);
            //undo the rotation
            g2.rotate(Math.PI/2);
            //to draw the grid translate back
            g2.translate(-xOffset, -yOffset);

            //draw cross across screen to indicate center ofcomponent

            Line2D line = new Line2D.Double(0,0, getWidth(),getHeight());
            g2.draw(line);

            line = new Line2D.Double(getWidth(),0,0,getHeight());
            g2.draw(line);



            g2.translate(this.canvasWidth - 40, 0);
            paintScale(g2, 40);
            g2.translate(-this.canvasWidth + 40, 0);

        }


	}

    private void  paintScale( Graphics2D g2, int width){
        //draw the gradient according to the values returned by the current colormap
        for( int i = 0; i < this.getHeight(); i++ ){
            double range = Math.abs(maxValueInData - minValueInData);
            double value = minValueInData  + ( ((double)i)/this.getHeight() )  * range;
            Color c = this.colormap.getColorFromValue(value, minValueInData, maxValueInData);
            g2.setColor( c );
            g2.drawLine(20, this.getHeight() - i, width, this.getHeight() - i);
            //draw a number next to the colorbar each 64 pixel
            if(i > 0 && (i % 70) == 0){
                g2.setColor(Color.GRAY);
                g2.drawString(fmt.format(value), -25, this.getHeight() - i);
            }
        }
        //now draw some numbers next to it
        g2.setColor(Color.WHITE);
        g2.drawString(fmt.format(minValueInData), -25, this.getHeight() - 5);
        g2.drawString(fmt.format(maxValueInData), -25,  10);

    }





    @Override
    public void mouseClicked(MouseEvent arg0) {
        if (arg0.getButton() == MouseEvent.BUTTON1) {
            for (Tile cell : tiles) {
                //since we transformed the geometry while painting the polygons above we now have to transform
                // the coordinates of the mouse pointer.
                Point p = arg0.getPoint();
                p.translate(-getWidth()/2, -getHeight()/2);
                AffineTransform rotateInstance = AffineTransform.getRotateInstance(Math.PI/2);
                rotateInstance.transform(p, p);


                if (cell.contains(p)) {
                    CameraPixel selectedPixel = cell.getCameraPixel();
                    boolean shiftDown = arg0.isShiftDown();

                    if (shiftDown && selectedPixels.contains(selectedPixel)) {
                        selectedPixels.remove(selectedPixel);
                    } else {
                        if (!shiftDown) {
                            selectedPixels.clear();
                        }
                        selectedPixels.add(selectedPixel);
                    }
                    Bus.eventBus.post(selectedPixels);
                    this.repaint();
                    return;
                }
            }
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }


    // The update method. to update.
    @Override
	public void update(Graphics g) {
		super.paint(g);
		this.paint(g);
	}



    //--------- swing overrides------------
    /**
     * @see javax.swing.JComponent#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(getWidth(), getHeight()); // getHeight(),
    }

    /**
     * @see javax.swing.JComponent#getHeight()
     */
    @Override
    public int getHeight() {
        return this.canvasHeight;
        //return (int) (this.cellHeight * (rows) );
    }


    /**
     * @see javax.swing.JComponent#getWidth()
     */
    @Override
    public int getWidth() {
        return this.canvasWidth;
        //System.out.println("ads " + (int) (this.cellWidth * (cols) + 100) );
        //return (int) (this.cellWidth * (cols) );
    }

    /**
     * @see javax.swing.JComponent#getMaximumSize()
     */
    @Override
    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }




	//------Getter and Setter----------------
    @Override
    public int getNumberOfTiles() {
        return tiles.length;
    }
}
