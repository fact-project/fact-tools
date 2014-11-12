package fact.cleaning.snake;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URL;
import java.util.Arrays;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.junit.Test;

import stream.Data;
import stream.data.DataFactory;

public class SnakeTest extends Snake
{
	@Test
	public void testSnake() throws Exception
	{

		Data item = DataFactory.create();

		CreateGaus gaus = new CreateGaus();
		gaus.setSigmaX(15);
		gaus.setSigmaY(33);

		gaus.setX0(-22);
		gaus.setY0(45);

		gaus.setOutputKey("gaus");
		gaus.process(item);

		SnakeSingle sn = new SnakeSingle();

		sn.setForceName("Photoncharge");

		sn.init(null);

		sn.setPixelDataName("gaus");

		item.put("Mean", 0.5);
		sn.setMean("Mean");

		item.put("centerX", -22.0);
		sn.setShowerCenterX("centerX");

		item.put("centerY", 45.0);
		sn.setShowerCenterY("centerY");

		sn.setSnakeOutX("snakeOutX");
		sn.setSnakeOutY("snakeOutY");
		sn.setNumberOfVerticesOut("numberOfVertices");

		sn.process(item);

		int nov = (Integer) item.get("numberOfVertices");

		assertTrue("Snake numberOfVertices kleiner 6", 6 < nov);

	}

	@Test
	public void testMatrix() throws Exception
	{
		this.initConstant(0.03, 0.01, 0.08, 1.0);
		this.initMatrix();

		for(int i = 0; i < _MAX_VERTICES; i++)
		{

			// Manual Check output:
			DoubleMatrix tmp = calcMatrix(i + 1, 0.03, 0.01, 0.08, 1.0);	
			if( i < 8 )
			{
				System.out.println("Matrix " + i + ": ");
				for(int y = 0; y < i + 1; y++)
				{
					for(int x = 0; x < i + 1; x++)
					{
						System.out.print(tmp.data[x + y * (i + 1)] + " ");
					}
					System.out.println("");
				}
			}
			
			
			tmp = tmp.mmul(matrix[i]);
			
			
			for(int y = 0; y < i + 1; y++)
			{
				for(int x = 0; x < i + 1; x++)
				{
					if( x == y )
					{
						assertEquals("Diagonal Element should be 1",
								tmp.get(x, y), 1.0, 0.0001);
					}
					else
					{
						assertEquals("Non Diagonal Elements should be 0",
								tmp.get(x, y), 0.0, 0.0001);
					}
				}

			}
		}
	}

	protected DoubleMatrix calcMatrix(int count, double alpha, double beta,
			double dt, double ds2)
	{
		final double a = alpha * dt / ds2;
		final double b = beta * dt / ds2;

		final double r = 1.0 + (2.0 * a) + (6.0 * b);
		final double p = b;
		final double q = (-a) - (4.0 * b);

		DoubleMatrix matrix = new DoubleMatrix(count, count);
		for(int i = 0; i < count; i++)
		{
			matrix.put(i, i, r);

			matrix.put((i + 1) % count, i, q);
			matrix.put((i + 2) % count, i, p);

			matrix.put(((i - 1) + count) % count, i, q);
			matrix.put(((i - 2) + count) % count, i, p);
		}

		return matrix;
	}

}
