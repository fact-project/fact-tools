package fact.features.snake;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.MathUtils;
import org.apache.commons.math3.linear.DecompositionSolver;

import fact.EventUtils;
import stream.Data;
import stream.Processor;

/*
public class fitEllipse  implements Processor
{
	String outkey;	

	@Override
	public Data process(Data input) 
	{
		EventUtils.mapContainsKeys(getClass(), input, "snake_X", "snake_Y");		
		double[] x = (double[]) input.get("snake_X");
		double[] y = (double[]) input.get("snake_Y");
		
		int size = x.length;
		
		RealMatrix D1 = new Array2DRowRealMatrix(size,3);
		RealMatrix D2 = new Array2DRowRealMatrix(size,3);
		
		for(int i=0; i<size; i++)
		{
			D1.setEntry(i, 0, x[i] * x[i]) ;
			D1.setEntry(i, 1, x[i] * y[i]) ;
			D1.setEntry(i, 2, y[i] * y[i]) ;

			D2.setEntry(i, 0, x[i]) ;
			D2.setEntry(i, 1, y[i]) ;
			D2.setEntry(i, 2, 1) ;
		}
		//std::cout<<"D1: \n"<<D1<<std::endl<<std::endl;	
		//std::cout<<"D2: \n"<<D2<<std::endl<<std::endl;


		RealMatrix S1 = D1.transpose().multiply(D1);
		RealMatrix S2 = D1.transpose().multiply(D2);
		RealMatrix S3 = D2.transpose().multiply(D2);

		//std::cout<<"S1: \n"<<S1<<std::endl<<std::endl;
		//std::cout<<"S2: \n"<<S2<<std::endl<<std::endl;
		//std::cout<<"S3: \n"<<S3<<std::endl<<std::endl;

		RealMatrix tmp = new QRDecomposition(S3).getSolver().getInverse();
		
		RealMatrix T = tmp.multiply(S2.transpose()).scalarMultiply(-1);
		
		RealMatrix M = S1.add(S2.multiply(T));

		RealMatrix C1 = new Array2DRowRealMatrix(3,3);
		C1.setEntry(0, 0, 0);
		C1.setEntry(0, 1, 0);
		C1.setEntry(0, 2, 0.5);
		C1.setEntry(1, 0, 0);
		C1.setEntry(1, 1, -1);
		C1.setEntry(1, 2, 0);
		C1.setEntry(2, 0, 0.5);
		C1.setEntry(2, 1, 0);
		C1.setEntry(2, 2, 0);

		//std::cout<<"C1: \n"<<C1<<std::endl<<std::endl;

		M = C1.multiply(M);

		//DecompositionSolver eigenSys = new QRDecomposition(M).getSolver();	
	
		EigenDecomposition eigenSys = new EigenDecomposition(M);
		
		

		//Eigen::MatrixXcd eigenVector = eigenSys.eigenvectors();
		//Eigen::MatrixXcd eigenValue  = eigenSys.eigenvalues();

		RealMatrix a1 = new Array2DRowRealMatrix(3,1);

		RealVector[] eigenVec = new RealVector[3];
		eigenVec[0] = eigenSys.getEigenvector(0);
		eigenVec[1] = eigenSys.getEigenvector(1);
		eigenVec[2] = eigenSys.getEigenvector(2);
		
		for(int i = 0; i< 3; i++)
		{
			double cond = 4.0*(eigenVec[0].getEntry(0)*eigenVec[0].getEntry(2)) - eigenVec[0].getEntry(1)*eigenVec[0].getEntry(1);			

			if(cond > 0)
			{				
				for(int j=0; j<eigenVec[i].getDimension(); j++)
				{					
					a1.setEntry(j, 0, eigenVector[i].getEntry(j));
				}			
			}
		}

		Eigen::Vector3d a2 = T * a1;

		m_parameter[0] = a1[0];
		m_parameter[1] = a1[1];
		m_parameter[2] = a1[2];
		m_parameter[3] = a2[0];
		m_parameter[4] = a2[1];
		m_parameter[5] = a2[2]; 
		
		return input;
	}

	public String getOutkey() {
		return outkey;
	}

	public void setOutkey(String outkey) {
		this.outkey = outkey;
	}
	
}*/
