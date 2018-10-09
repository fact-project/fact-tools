package fact.starservice;

import fact.coordinates.EquatorialCoordinate;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class StarServiceTest {

    @Test
    public void testLoadCatalog() {
        StarService starService = new StarService();
        assertEquals(5044, starService.catalog.length);
    }

    @Test
    public void testCrabNebulaMag6() {
        StarService starService = new StarService();

        // standard wobble position for crab
        EquatorialCoordinate pointing = EquatorialCoordinate.fromDegrees(84.12915, 22.3994);
        Star[] starsInFOV = starService.getStarsInFov(pointing, 6.5);
        Arrays.sort(starsInFOV);

        assertEquals("Standard Crab Pointing position should have 3 bright stars in FOV", 3, starsInFOV.length);
        assertEquals("Brightest star in Crab Nebula FOV should be Zeta Tauri", 26451, starsInFOV[0].id);
        assertEquals("Second brightest star in Crab Nebula FOV should be o Tauri", 25539, starsInFOV[1].id);
        assertEquals("Third brightest star in Crab Nebula FOV should be 121 Tauri", 26248, starsInFOV[2].id);
    }

    @Test
    public void testCrabNebulaMag4() {
        StarService starService = new StarService();

        // standard wobble position for crab
        EquatorialCoordinate pointing = EquatorialCoordinate.fromDegrees(84.12915, 22.3994);
        Star[] starsInFOV = starService.getStarsInFov(pointing, 4);

        assertEquals("Standard Crab Pointing position should have 1 star brighter 4 in FOV", 1, starsInFOV.length);
        assertEquals("Brightest star in Crab Nebula FOV should be Zeta Tauri", 26451, starsInFOV[0].id);
    }
}
