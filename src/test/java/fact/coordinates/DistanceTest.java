package fact.coordinates;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


/**
 * Created by maxnoe on 22.05.17.
 */
public class DistanceTest {

    @Test
    public void HorizontalDistance1() {

        HorizontalCoordinate p1 = HorizontalCoordinate.fromDegrees(90, 0);
        HorizontalCoordinate p2 = HorizontalCoordinate.fromDegrees(0, 0);

        assertEquals(Math.PI / 2, p1.greatCircleDistanceRad(p2), 1e-9);

    }

    @Test
    public void HorizontalDistance2() {

        HorizontalCoordinate p1 = HorizontalCoordinate.fromDegrees(0, 90);
        HorizontalCoordinate p2 = HorizontalCoordinate.fromDegrees(0, 270);

        assertEquals(0.0, p1.greatCircleDistanceRad(p2), 1e-9);

    }

    @Test
    public void EquatorialDistance1() {

        EquatorialCoordinate crab = EquatorialCoordinate.fromRad(1.459674920399749, 0.38422481179392015);
        EquatorialCoordinate mrk501 = EquatorialCoordinate.fromRad(4.423843636547886, 0.6939458636900399);

        assertEquals(2.050766865003829, crab.greatCircleDistanceRad(mrk501), 1e-9);

    }
}
