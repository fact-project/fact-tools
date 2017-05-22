package fact.coordinates;

import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;


/**
 * Created by maxnoe on 22.05.17.
 */
public class DistanceTests {

    @Test
    public void HorizontalDistance1() {

        HorizontalCoordinate p1 = HorizontalCoordinate.fromDegrees(90, 0);
        HorizontalCoordinate p2 = HorizontalCoordinate.fromDegrees(0, 0);

        assertEquals(Math.PI / 2, p1.greatCircleDistance(p2), 1e-9);

    }

    @Test
    public void HorizontalDistance2() {

        HorizontalCoordinate p1 = HorizontalCoordinate.fromDegrees(0, 90);
        HorizontalCoordinate p2 = HorizontalCoordinate.fromDegrees(0, 270);

        assertEquals(0.0, p1.greatCircleDistance(p2), 1e-9);

    }

    @Test
    public void EquatorialDistance1() {

        EquatorialCoordinate crab = new EquatorialCoordinate(1.459674920399749, 0.38422481179392015);
        EquatorialCoordinate mrk501 = new EquatorialCoordinate(4.423843636547886, 0.6939458636900399);

        assertEquals(2.050766865003829, crab.greatCircleDistance(mrk501), 1e-9);

    }

    @Test
    public void TransformationDistance1() {

        EquatorialCoordinate crab = new EquatorialCoordinate(1.459674920399749, 0.38422481179392015);
        EquatorialCoordinate mrk501 = new EquatorialCoordinate(4.423843636547886, 0.6939458636900399);

        ZonedDateTime observationTime = ZonedDateTime.of(2014, 3, 25, 00, 42, 29, 0, ZoneOffset.UTC);
        EarthLocation FACTLocation = EarthLocation.fromDegrees(28.761795, -17.890701389, 2200);

        HorizontalCoordinate crabHorizontal = crab.toHorizontal(observationTime, FACTLocation);
        HorizontalCoordinate mrk501Horizontal = mrk501.toHorizontal(observationTime, FACTLocation);

        assertEquals(2.050766865003829, crabHorizontal.greatCircleDistance(mrk501Horizontal), 1e-12);

    }
}
