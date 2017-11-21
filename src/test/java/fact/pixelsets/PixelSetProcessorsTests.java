package fact.pixelsets;

import fact.container.PixelSet;
import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PixelSetProcessorsTests {



    @Test
    public void createFullCameraSetTest(){
        Invert i = new Invert();
        PixelSet fullCameraSet = i.createFullCameraSet(1440);
        assertThat(fullCameraSet.set.size(), is(1440));
    }

    @Test
    public void invertTest(){
        Data item = DataFactory.create();

        PixelSet testSet = new PixelSet();
        for (int pix = 0; pix < 20; pix++) {
            testSet.addById(pix);
        }
        item.put("testSet", testSet );
        item.put("NPIX", 1440);

        Invert invert = new Invert();

        invert.insetKey = "testSet";
        invert.outsetKey = "testInversion";
        item = invert.process(item);
        assertThat(item.containsKey("testInversion"), is(true));


        PixelSet inversion = (PixelSet) item.get("testInversion");
        assertEquals(1440 - 20, inversion.size());
    }

    @Test
    public void intersectionTest(){
        Data item = DataFactory.create();

        PixelSet setA = PixelSet.fromIDs(new int[] {1, 2, 3});
        PixelSet setB = PixelSet.fromIDs(new int[] {2, 3, 4, 5});
        PixelSet setIntersection = PixelSet.fromIDs(new int[] {2, 3});

        item.put("setA", setA);
        item.put("setB", setB);

        Intersection intersection = new Intersection();
        intersection.setAKey = "setA";
        intersection.setBKey = "setB";
        intersection.outsetKey = "testIntersection";
        item = intersection.process(item);

        assertTrue(item.containsKey("testIntersection"));

        PixelSet unionResult = (PixelSet) item.get("testIntersection");
        assertEquals(2, unionResult.size());
        assertTrue(unionResult.containsAll(setIntersection));
    }

    @Test
    public void testEmptyIntersection(){
        Data item = DataFactory.create();

        PixelSet setA = new PixelSet();
        PixelSet setB = new PixelSet();


        Intersection intersection = new Intersection();
        intersection.setAKey = "setA";
        intersection.setBKey = "setB";
        intersection.outsetKey = "testIntersection";

        item.put("setA", setA);
        item.put("setB", setB);
        item = intersection.process(item);

        assertTrue(item.containsKey("testIntersection"));

        PixelSet unionResult = (PixelSet) item.get("testIntersection");
        assertEquals(0, unionResult.size());

    }

    @Test(expected = RuntimeException.class)
    public void testMissingKeyIntersection(){
        Data item = DataFactory.create();


        Intersection intersection = new Intersection();
        intersection.setAKey = "setA";
        intersection.setBKey = "setB";
        intersection.outsetKey = "testIntersection";

        intersection.process(item);
    }

    @Test
    public void unionTest(){
        Data item = DataFactory.create();

        PixelSet setA = PixelSet.fromIDs(new int[] {1, 2, 3});
        PixelSet setB = PixelSet.fromIDs(new int[] {3, 4, 5});
        PixelSet setUnion = PixelSet.fromIDs(new int[] {1, 2, 3, 4, 5});

        item.put("setA", setA);
        item.put("setB", setB);

        Union union = new Union();
        union.setAKey = "setA";
        union.setBKey = "setB";
        union.outsetKey = "testUnion";
        item = union.process(item);

        assertTrue(item.containsKey("testUnion"));

        PixelSet unionResult = (PixelSet) item.get("testUnion");
        assertEquals(5, unionResult.size());
        assertTrue(unionResult.containsAll(setUnion));
    }

}
