package play;

import java.util.Iterator;
import java.util.List;

import com.winvector.linalg.DenseVec;
import com.winvector.linalg.Matrix;
import com.winvector.linalg.colt.NativeMatrix;
import com.winvector.lp.LPEQProb;
import com.winvector.lp.LPException;
import com.winvector.lp.LPSoln;
import com.winvector.lp.LPException.LPMalformedException;

import gametree.GameNode;
import gametree.LinearProgrammingProblem;
import gametree.PayOff;
import lp.LinearProgramming;
import lp.getSubSets;
import play.exception.InvalidStrategyException;

public class G6Strategy extends Strategy {
	
	private PayOff[][] gameTable = null;
	
	private double[] setLP() {
		
		LinearProgrammingProblem problem = new LinearProgrammingProblem();	
		
		int p1Moves = gameTable.length;
		int p2Moves = gameTable[0].length;
		
		
		
		int subSetCounter = Math.min(p1Moves, p2Moves);
		
		boolean sol = false;
		while(!sol) {
			
			List<boolean[]> p1Subsets = getSubSets.getSubsets(0, subSetCounter, p1Moves);
			
			
			
			getSubSets.showSubSet(p1Subsets);
			
			
			
			for(boolean[] setP1: p1Subsets) {
			
				
					
				
				List<boolean[]> p2Subsets = getSubSets.getSubsets(0, subSetCounter, p2Moves);
				getSubSets.showSubSet(p2Subsets);
				
				int numVars = p1Moves + p2Moves + 2;
				int numSlacks = p1Moves + p2Moves;
				int numConstraints = p1Moves*2 + p2Moves*2 + 2;
				int totalVarsLP = numVars + numConstraints;
				int posU = numVars -2;
				int posV = numVars -1;
				
				Matrix<NativeMatrix> m = NativeMatrix.factory.newMatrix(numConstraints,totalVarsLP,false);
				
				final double[] b = new double[numConstraints];

				final double[] c = new double[totalVarsLP];
				
				int counterConst = 2;
				int counterSlack = numVars;
				
				
				for (int i = 0; i < p1Moves; i++) {
					
					if(setP1[i]) {
						m.set(0, i, 1);
						b[0] = 1;
						
						m.set(counterConst, i, -1); m.set(counterConst, counterSlack, 1);
						
						counterConst++;
						counterSlack++;
					} else {
						m.set(counterConst, i, 1);
						
						counterConst++;
					}
				}
				
				for (int i = 0; i < p1Moves; i++) {
					for (int j = 0; j < p2Moves; j++) {
				
						if(setP1[i]) {
							m.set(counterConst , j+p1Moves, gameTable[i][j].payoffP1);

							m.set(counterConst, posU, -1);

						} else {
							m.set(counterConst , j+p1Moves, -gameTable[i][j].payoffP1);
							m.set(counterConst, counterSlack, 1);
							m.set(counterConst, posU, 1);
							
//							counterSlack++;
//							counterConst++;
						}
						
					}
					counterConst++;
					
					if(!setP1[i]) {
						counterSlack++;
					}
						
				}
				
				int oldCC = counterConst;
				int oldCS = counterSlack;
				
				Matrix<NativeMatrix> m_old = m.copy(NativeMatrix.factory, false);
				

				for (boolean[]  setP2: p2Subsets) {
					
					m = m_old.copy(NativeMatrix.factory, false);
					counterConst = oldCC;
					counterSlack = oldCS;
					
					for (int i = 0; i < p2Moves; i++) {
						
						if(setP2[i]) {
							m.set(1, i+p1Moves, 1);
							b[1] = 1;
							
							
							for (int x = 0; x < m.rows(); x++) {
								for (int j = 0; j < m.cols(); j++) {
									System.out.print(m.get(x, j)+" ") ;
								}
								
							}
							
							m.set(counterConst, i + p1Moves, -1);
							
							
							m.set(counterConst, counterSlack, 1);
							counterConst++;
							counterSlack++;
						} else {
							m.set(counterConst, i + p1Moves, 1);
							
							counterConst++;
						}
					}
					
					for (int i = 0; i < p2Moves; i++) {
						for (int j = 0; j < p1Moves; j++) {
					
							if(setP2[i]) {
								m.set(counterConst , j, gameTable[j][i].payoffP2);
								m.set(counterConst, posV, -1);
								
							} else {
								m.set(counterConst , j, -gameTable[j][i].payoffP2);
								m.set(counterConst, counterSlack, 1);
								m.set(counterConst, posV, 1);
							}
							
						}
						counterConst++;
						
						if(!setP2[i]) {
							counterSlack++;
						}
							
					}
				}
				c[posU] = 1;
				c[posV] = 1;
				
				
				problem.numVariables = numVars;
				problem.numConstraints = numConstraints;
				
				problem.m = m;
				problem.b = b;
				problem.c = c;
				
				try {
					problem.prob = new LPEQProb(m.columnMatrix(),b,new DenseVec(c));
				}
				catch (LPMalformedException e) {
					System.out.println("Error in problem specification!");
					e.printStackTrace();
				}
				
				double[] solution = null;
				try {
					solution = LinearProgramming.linearProgramming(problem);
				} catch (LPException e) {
					e.printStackTrace();
				}
				
				if(solution != null)
					return solution;
				
			}
			
			subSetCounter --;
		}
		return null;
	}
	
	
	
	private void readTree() {
	
		List<Integer> list = this.tree.getValidationSet();
		gameTable = new PayOff[list.get(0)][list.get(1)];
		

		GameNode root = this.tree.getRootNode();

		Iterator<GameNode> itP1 = root.getChildren();
		int countP1Moves = 0;
		int countP2Moves = 0;
		
		while (itP1.hasNext()) {
			countP2Moves = 0;
			GameNode p1Node = itP1.next();
			Iterator<GameNode> itP2 = p1Node.getChildren();
			
			while (itP2.hasNext()) {
				GameNode p2Node = itP2.next();
				
				gameTable[countP1Moves][countP2Moves] = new PayOff(p1Node.getLabel(), p2Node.getLabel(), p2Node.getPayoffP1(), p2Node.getPayoffP2());

				countP2Moves++;
				
			}
			
			countP1Moves++;
		}
		
	}

	@Override
	public void execute() throws InterruptedException {
				
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		
		this.readTree();
		
		double[] nashEq = setLP();
		
		while(true) {
		
			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
						
			
			while(! playComplete ) {
				if(myStrategy.getFinalP1Node() != -1) {
					GameNode finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}
				
				if(myStrategy.getFinalP2Node() != -1) {
					GameNode finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}
				
				
				for(int i = 0; i < gameTable.length; i++) {
					myStrategy.put(gameTable[i][0].p1Move, nashEq[i]);
				}
				
				for(int j = 0; j < gameTable[0].length; j++) {
					myStrategy.put(gameTable[0][j].p2Move, nashEq[j+gameTable.length]);
				}
				Iterator<String> keys = myStrategy.keyIterator();
				
				try{
					this.provideStrategy(myStrategy);
					playComplete = true;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());;
					e.printStackTrace(System.err);
				} 
				
			}
		}
		
	}

}
