package fact.camera;

        import fact.hexmap.CameraPixel;
        import fact.hexmap.FactCameraPixel;
        import fact.hexmap.FactPixelMapping;
        import org.junit.Test;

        import java.io.File;
        import java.io.FileWriter;
        import java.io.IOException;
        import java.util.ArrayList;

        import static org.junit.Assert.*;

/**
 * Created by lena on 03.12.15.
 */
public class CubeCoordinateTest {
    FactPixelMapping mapping = FactPixelMapping.getInstance();

    @Test
    public void testPosition() {
/*        for(int i=0; i<1440; i++){
            int [] cube = mapping.getCubeCoordinatesFromId(i);
            FactCameraPixel p = mapping.getPixelFromCubeCoordinates(cube[0], cube[1], cube[2]);
            //System.out.println(p.id);

        }*/
        FactCameraPixel p = mapping.getPixelFromOffsetCoordinates(-18, -11);
//        System.out.println(p.id);
    }

    @Test
    public void testLine(){
        //line(5,26);
        line(946, 1020);
    }

    public ArrayList<Integer> line(int id1, int id2) {

            ArrayList<Integer> line = new ArrayList<>();

            int[] cube1 = mapping.getCubeCoordinatesFromId(id1);
            int[] cube2 = mapping.getCubeCoordinatesFromId(id2);

            int hexDistance = (Math.abs(cube2[0] - cube1[0]) + Math.abs(cube2[1] - cube1[1]) + Math.abs(cube2[2] - cube1[2])) / 2;

            //System.out.println(hexDistance);
            double N = (double) hexDistance;

            for (int i = 0; i <= hexDistance; i++) {
                double[] point = linePoint(cube1, cube2, 1.0 / N * i);
                int[] pixel = cube_round(point);
                FactCameraPixel linePixel = mapping.getPixelFromCubeCoordinates(pixel[0], pixel[2]);
                line.add(linePixel.id);
            }

        return line;
    }

    private double[] linePoint(int[] cube1, int [] cube2, double t){
        double [] linePoint = new double[3];
        linePoint[0] = (double) cube1[0] + ((double) cube2[0] - (double)cube1[0])*t;
        linePoint[1] = (double) cube1[1] + ((double) cube2[1] - (double)cube1[1])*t;
        linePoint[2] = (double) cube1[2] + ((double) cube2[2] - (double)cube1[2])*t;

        return linePoint;
    }

    private int[] cube_round(double [] linePoint){
        int rx = (int) Math.round(linePoint[0]);
        int ry = (int) Math.round(linePoint[1]);
        int rz = (int) Math.round(linePoint[2]);

        double x_diff = Math.abs(rx - linePoint[0]);
        double y_diff = Math.abs(ry - linePoint[1]);
        double z_diff = Math.abs(rz - linePoint[2]);

        if(x_diff > y_diff && x_diff > z_diff){
            rx = -ry - rz;
        }
        else if(y_diff > z_diff){
            ry = -rx - rz;
        }
        else {
            rz = -rx - ry;
        }

        int [] linePixel = new int [3];
        linePixel[0] = rx;
        linePixel[1] = ry;
        linePixel[2] = rz;

        return linePixel;
    }
}
