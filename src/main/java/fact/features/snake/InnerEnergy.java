package fact.features.snake;

import fact.Utils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import stream.Data;
import stream.Processor;
import stream.annotations.Parameter;

public class InnerEnergy implements Processor {

    @Parameter(required = true)
    private String snakeX = null;

    @Parameter(required = true)
    private String snakeY = null;

    @Parameter(required = true)
    private String outkey = null;

    @Override
    public Data process(Data item) {
        Utils.mapContainsKeys(item, snakeX, snakeY);

        double[] x = (double[]) item.get(snakeX);
        double[] y = (double[]) item.get(snakeY);


        final double b = 1;
        final double r = 1.0 + 6.0 * b;
        final double p = b;
        final double q = -4.0 * b;

        int dim = x.length;
        RealMatrix matrix = new Array2DRowRealMatrix(dim, dim);

        for (int i = 0; i < dim; i++) {
            matrix.setEntry(i, i, r);

            matrix.setEntry((i + 1) % dim, i, q);
            matrix.setEntry((i + 2) % dim, i, p);

            matrix.setEntry(((i - 1) + dim) % dim, i, q);
            matrix.setEntry(((i - 2) + dim) % dim, i, p);
        }

        matrix = new LUDecomposition(matrix).getSolver().getInverse();

        RealMatrix vecX = new Array2DRowRealMatrix(dim, 1);
        RealMatrix vecY = new Array2DRowRealMatrix(dim, 1);

        for (int i = 0; i < dim; i++) {
            vecX.setEntry(i, 0, x[i]);
            vecY.setEntry(i, 0, y[i]);
        }

        double erg = vecX.subtract(matrix.multiply(vecX)).getFrobeniusNorm();
        erg += vecY.subtract(matrix.multiply(vecY)).getFrobeniusNorm();

        erg /= 2.0 * dim;

        item.put(outkey, erg);
        return item;
    }
}
