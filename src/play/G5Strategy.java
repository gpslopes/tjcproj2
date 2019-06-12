package play;

import java.util.ArrayList;
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

public class G5Strategy extends Strategy {
	
	private PayOff[][] gameTable = null;
	
	private String[] getNashSW(){
		
		String[] nashEq = new String[2];
		
		double bestSumPayoff = -Double.MAX_VALUE;
		
		for (int i = 0; i < gameTable.length; i++) {
			for (int j = 0; j < gameTable[0].length; j++) {
				
				double payoffP1 = gameTable[i][j].payoffP1;
				double payoffP2 = gameTable[i][j].payoffP2;
				
				
				outerloop: for (int row = 0; row < gameTable.length; row++) {
					if(payoffP1 >= gameTable[row][j].payoffP1) {
						for (int col = 0; col < gameTable[0].length; col++) {
							if(payoffP2 < gameTable[i][col].payoffP2) {
								break outerloop;
							}
						}
						
						double sumP = payoffP1 + payoffP2;
						
						if(sumP > bestSumPayoff) {
							nashEq[0] = gameTable[i][j].p1Move;
							nashEq[1] = gameTable[i][j].p2Move;
							bestSumPayoff = sumP;
							
						}
					}
					else {
						break outerloop;
					}
				}
			}
		}
		 
		return nashEq;
	}
	
	
	private void readTree() {
	
		List<Integer> list = this.tree.getValidationSet();
		gameTable = new PayOff[list.get(0)][list.get(1)];
		
		for (Integer integer : list) {
			System.out.println("List: " + integer);
		}
		System.out.println("HERE");
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
		
		String[] sol = getNashSW();
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
				
				Iterator<String> keys = myStrategy.keyIterator();
				
				while (keys.hasNext()) {
					
					String key = keys.next();
					if(key.equals(sol[0]) || key.equals(sol[1])) {
						myStrategy.put(key, 1.0);
					} else {
						myStrategy.put(key, 0.0);
					}
						
					
				}
				
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
