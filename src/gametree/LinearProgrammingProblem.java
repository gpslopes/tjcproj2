package gametree;

import com.winvector.linalg.Matrix;
import com.winvector.linalg.colt.NativeMatrix;
import com.winvector.lp.LPEQProb;
import com.winvector.lp.LPSoln;

public class LinearProgrammingProblem {

	public int numVariables;
	public int numConstraints;
	public boolean feasible;
	public boolean bounded;
	public Matrix<NativeMatrix> m; 
	public double[] b; 
	public double[] c;
	public LPEQProb prob;
	public LPSoln solution;
}
