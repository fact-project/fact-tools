package fact;

import fact.coordinates.CameraCoordinate;
import fact.hexmap.ui.Viewer;
import fact.hexmap.ui.overlays.EllipseOverlay;
import fact.hexmap.ui.overlays.SourcePositionOverlay;
import fact.io.hdureader.BinTable;
import fact.io.hdureader.BinTableReader;
import fact.io.hdureader.FITS;
import fact.io.hdureader.OptionalTypesMap;
import stream.Data;
import stream.data.DataFactory;

import java.io.File;
import java.io.Serializable;

public class ViewerManualTest {

    public static void main(String[] args) throws Exception {

        Data item = DataFactory.create();

        FITS fits = FITS.fromFile(new File("src/main/resources/testDataFile.fits.gz"));
        BinTable table = fits.getBinTableByName("Events").orElseThrow(RuntimeException::new);
        BinTableReader reader = BinTableReader.forBinTable(table);

        OptionalTypesMap<String, Serializable> event = null;
        for (int i = 0; i < 11 ; i++) {
            event = reader.getNextRow();

        }
        short[] data = event.getShortArray("Data").orElseThrow(RuntimeException::new);


        item.put("Data", data);

        item.put("sourceMarker", new SourcePositionOverlay("test", new CameraCoordinate(0, 50)));

        item.put("e1", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(0)));
        item.put("e2", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(22.5)));
        item.put("e3", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(45)));
        item.put("e4", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(90)));
        item.put("e5", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(135)));
        item.put("e6", new EllipseOverlay(0, 0, 50, 25, Math.toRadians(225)));

        Viewer viewer = Viewer.getInstance();
        viewer.setVisible(true);
        viewer.setDefaultKey("Data");
        viewer.setDataItem(item);

    }
}
