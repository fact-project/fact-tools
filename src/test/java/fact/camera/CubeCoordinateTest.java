package fact.camera;

        import fact.hexmap.CameraPixel;
        import fact.hexmap.FactCameraPixel;
        import fact.hexmap.FactPixelMapping;
        import org.junit.Test;

        import java.io.*;
        import java.util.ArrayList;

        import static org.junit.Assert.*;

/**
 * Created by lena on 03.12.15.
 */
public class CubeCoordinateTest {
    FactPixelMapping mapping = FactPixelMapping.getInstance();

/*    @Test
    public void testPosition() {
        double [] xPosition = new double [1440];
        double [] yPosition = new double [1440];
        for(int i=0; i<1440; i++){
            int [] cube = mapping.getCubeCoordinatesFromId(i);
            FactCameraPixel p = mapping.getPixelFromCubeCoordinates(cube[0], cube[2]);
            xPosition[p.id] = p.posX;
            yPosition[p.id] = p.posY;
        }
       int i =0;

//        System.out.println(p.id);

    }*/
    @Test
    public void testLine(){
            ArrayList<Integer> line1 = mapping.line(1128,378);
            ArrayList<Integer> line2 = mapping.line(295,574);
            assertTrue("Wrong line pixel " + line1.get(1) +". Should be id 1126.", line1.get(1)==1126);
            assertTrue("Wrong line pixel " + line1.get(2) +". Should be id 1114.", line1.get(2)==1114);
            assertTrue("Wrong line pixel " + line1.get(3) +". Should be id 1100.", line1.get(3)==1100);
            assertTrue("Wrong line pixel " + line1.get(7) +". Should be id 388.", line1.get(7)==388);
            assertTrue("Wrong line pixel " + line1.get(8) +". Should be id 383.", line1.get(8)==383);
            assertTrue("Wrong line pixel " + line1.get(9) +". Should be id 380.", line1.get(9)==380);

            assertTrue("Wrong line pixel " + line2.get(1) +". Should be id 329.", line2.get(1)==329);
            assertTrue("Wrong line pixel " + line2.get(2) +". Should be id 330.", line2.get(2)==330);
            assertTrue("Wrong line pixel " + line2.get(3) +". Should be id 342.", line2.get(3)==342);
            assertTrue("Wrong line pixel " + line2.get(7) +". Should be id 460.", line2.get(7)==460);
            assertTrue("Wrong line pixel " + line2.get(8) +". Should be id 463.", line2.get(8)==463);
            assertTrue("Wrong line pixel " + line2.get(9) +". Should be id 497.", line2.get(9)==497);
    }

    /*public ArrayList<Integer> line(int id1, int id2) {

            ArrayList<Integer> line = new ArrayList<>();
            //line.add(id1);
            int[] cube1 = mapping.getCubeCoordinatesFromId(id1);
            int[] cube2 = mapping.getCubeCoordinatesFromId(id2);

            int hexDistance = (Math.abs(cube2[0] - cube1[0]) + Math.abs(cube2[1] - cube1[1]) + Math.abs(cube2[2] - cube1[2])) / 2;

            //System.out.println(hexDistance);
            double N = (double) hexDistance;

            for (int i = 0; i < hexDistance; i++) {
                double[] point = linePoint(cube1, cube2, 1.0 / N * i);
                int[] pixel = cube_round(point);
                FactCameraPixel linePixel = mapping.getPixelFromCubeCoordinates(pixel[0], pixel[2]);
                line.add(linePixel.id);
                    System.out.println(linePixel.id);
            }

*//*            PrintWriter writer = new PrintWriter("/home/lena/Dokumente/Masterarbeit/pyPlot/testLine.txt", "UTF-8");
            for(int i=0;i<1440;i++){
                    if(line.contains(i)) {
                            writer.println(1);
                    }
                    else{
                            writer.println(0);
                    }
            }

            writer.close();*//*

        return line;
    }

    private double[] linePoint(int[] cube1, int [] cube2, double t){
        double [] linePoint = new double[3];
        linePoint[0] = (double) cube1[0] + ((double) cube2[0] - (double)cube1[0])*t;
        linePoint[1] = (double) cube1[1] + ((double) cube2[1] - (double)cube1[1])*t;
        linePoint[2] = (double) cube1[2] + ((double) cube2[2] - (double)cube1[2])*t;

        return linePoint;
    }

    private int[] cube_round(double [] linePoint) {
            int rx = (int) Math.round(linePoint[0]);
            int ry = (int) Math.round(linePoint[1]);
            int rz = (int) Math.round(linePoint[2]);

            double x_diff = Math.abs(rx - linePoint[0]);
            double y_diff = Math.abs(ry - linePoint[1]);
            double z_diff = Math.abs(rz - linePoint[2]);

            if (x_diff > y_diff && x_diff > z_diff) {
                    rx = -ry - rz;
            } else if (y_diff > z_diff) {
                    ry = -rx - rz;
            } else {
                    rz = -rx - ry;
            }

            int[] linePixel = new int[3];
            linePixel[0] = rx;
            linePixel[1] = ry;
            linePixel[2] = rz;

            return linePixel;
    }
*/
/*    @Test
        public void testY() throws FileNotFoundException, UnsupportedEncodingException {
            PrintWriter writer = new PrintWriter("/home/lena/Dokumente/Masterarbeit/pyPlot/testCubeY.txt", "UTF-8");
            for(int i=0;i<1440;i++){
                int[] cube1 = mapping.getCubeCoordinatesFromId(i);
                int row = mapping.getPixelFromId(i).geometricX;
                int col = mapping.getPixelFromId(i).geometricY;
                writer.println(i + "\t" + cube1[0] + "\t" + cube1[1] + "\t" + cube1[2] + "\t" + row + "\t" + col);
            }

            writer.close();
    }*/


}
