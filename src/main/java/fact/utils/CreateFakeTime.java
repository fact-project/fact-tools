package fact.utils;

import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

import java.util.Random;

public class CreateFakeTime implements Processor {

    @Parameter(required = true)
    String outputKey = null;

    long seed = 0;

    Random random = new Random(seed);

    long currentUnixTime = System.currentTimeMillis() / 1000L;
    int currentUnixSec = (int) currentUnixTime;
    int currentUnixMuSec = (int) ((currentUnixTime - (long) currentUnixSec) * 1000000);

    double probA = 7.294;
    double probTau = -0.03582;

    @Override
    public Data process(Data item) {

        int[] fakeEventTime = new int[2];

        int deltaT = random.nextInt(330000);
        boolean isValid = true;

        // Neumannsches Rückweisungsverfahren:
        while (isValid == false) {
            deltaT = random.nextInt(330000);
            double probability = probA * Math.pow(Math.E, probTau * deltaT);
            double randomCheck = random.nextDouble() * probA;
            if (randomCheck < probability) {
                isValid = true;
            }
        }

        currentUnixMuSec += deltaT;
        if (currentUnixMuSec > 1000000) {
            currentUnixSec += 1;
            currentUnixMuSec -= 1000000;
        }
        fakeEventTime[0] = currentUnixSec;
        fakeEventTime[1] = currentUnixMuSec;

        item.put(outputKey, fakeEventTime);

        return item;
    }
}
