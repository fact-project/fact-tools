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
    private String name;
    private CameraCoordinate source;


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
        Paint oldPaint = g2.getPaint();
        AffineTransform transform = g2.getTransform();

        g2.setColor(color);

        // lets draw a star symbol
        Point position = map.cameraCoordinateToPixels(source.xMM, source.yMM);
        g2.translate(position.x, position.y);
        Polygon p = createStar(4, 8, 6);
        g2.fill(p);

        g2.drawString(name, 10, 0);

        g2.setPaint(oldPaint);
        g2.setTransform(transform);
    }

    public static Polygon createStar(double inner, double outer, int rays) {
        int[] x = new int[2 * rays];
        int[] y = new int[2 * rays];
        double radius;
        for (int i = 0; i < rays * 2; i++) {
            if (i % 2 == 0) {
                radius = inner;
            } else {
                radius = outer;
            }

            x[i] = (int) Math.round(radius * Math.cos(i * Math.PI / rays));
            y[i] = (int) Math.round(radius * Math.sin(i * Math.PI / rays));
        }
        return new Polygon(x, y, rays * 2);
    }


    @Override
    public int getDrawRank() {
        return 2;
    }
}
