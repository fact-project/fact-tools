package fact.hexmap.ui.overlays;

import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.components.cameradisplay.FactHexMapDisplay;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by kaibrugge on 04.09.14.
 */
public class SourcePositionOverlay implements CameraMapOverlay {

    private Color color = Color.YELLOW;
    private String name = "";

    private int[] pointsx = {-5, 5, 0};
    private int[] pointsy = {0, 0, 10};

    private CameraCoordinate source = new CameraCoordinate(0, 0);


    public SourcePositionOverlay(String name, CameraCoordinate source) {
        this.name = name;
        this.source = source;
    }

    @Override
    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public void paint(Graphics2D g2, FactHexMapDisplay map) {
        double radius = map.getTileRadiusInPixels();
        double scalingX = 0.172 * radius;
        double scalingY = -0.184 * radius;

        Paint oldPaint = g2.getPaint();
        AffineTransform transform = g2.getTransform();

        g2.setColor(color);

        //lets draw a star symbol
        g2.translate(source.xMM * scalingX, source.yMM * scalingY);
        Polygon p = new Polygon(pointsx, pointsy, 3);
        g2.fill(p);

        g2.rotate(Math.PI / 2);
        g2.drawString(name, 10, 0);
        g2.rotate(Math.PI / 2);
        g2.translate(0, -7);
        g2.fill(p);


        g2.setPaint(oldPaint);
        g2.setTransform(transform);
    }

    @Override
    public int getDrawRank() {
        return 2;
    }
}
