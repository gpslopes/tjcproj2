package play;

import java.util.Iterator;
import java.util.List;

import org.omg.CORBA.PUBLIC_MEMBER;

import com.winvector.lp.LPException;

import gametree.GameNode;
import gametree.NormalFormProblem;
import gametree.PayOff;
import lp.IteratedDominance;
import play.exception.InvalidStrategyException;

public class G3Strategy extends Strategy {
	
	private PayOff[][] gameTable = null;
		
	private void readTree() {
	
		List<Integer> list = this.tree.getValidationSet();
		gameTable = new PayOff[list.get(0)][list.get(1)];
		
		GameNode root = this.tree.getRootNode();

		Iterator<GameNode> itP1 = root.getChildren();
		int countP1Moves = 0;
		int countP2Moves = 0;
		
		while (itP1.hasNext()) {
			
			GameNode p1Node = itP1.next();

			Iterator<GameNode> itP2 = p1Node.getChildren();
			
			while (itP2.hasNext()) {
				GameNode p2Node = itP2.next();
				
				gameTable[countP1Moves][countP2Moves] = new PayOff(p1Node.getLabel(), p2Node.getLabel(), p2Node.getPayoffP1(), p2Node.getPayoffP2());

				countP2Moves++;
				
			}
			
			countP2Moves = 0;
			countP1Moves++;
		}
		System.out.println("END");
		
	}

	@Override
	public void execute() throws InterruptedException {
				
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		
		this.readTree();
		NormalFormProblem result = null;
		
		try {
			result = IteratedDominance.iteratedDominanceNF(gameTable);
		} catch (LPException e1) {
			e1.printStackTrace();
		}
		
		List<String> usedRows = result.getSelectedRows();
		List<String> usedCols = result.getSelectedCols();
		
		
		PayOff[][] nashTable = new PayOff[2][2];
		
		for (int i = 0; i < nashTable.length; i++) {
			
			for (int j = 0; j < nashTable[0].length; j++) {
				
				int rowPos = result.rowMoves.indexOf(usedRows.get(i));
				int colPos = result.colMoves.indexOf(usedCols.get(j));
				nashTable[i][j] = new PayOff(usedRows.get(i), usedCols.get(j), result.u1[rowPos][colPos], result.u2[rowPos][colPos]);
			}
			
		}
		
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
				
				while(keys.hasNext()) {
					String key = keys.next();
					if(usedRows.contains(key)) {
						double p = calculateNEP1(nashTable);
						
						if(usedRows.indexOf(key) == 0) {
							myStrategy.put(key, p);
						}
						else {
							myStrategy.put(key, 1-p);
						}
					}
					else if(usedCols.contains(key)){
						double q = calculateNEP2(nashTable);
						
						if(usedCols.indexOf(key) == 0) {
							myStrategy.put(key, q);
						}
						else {
							myStrategy.put(key, 1-q);
						}
					}
					else
						myStrategy.put(key, 0.0);
						
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
	
	public double calculateNEP2(PayOff[][] nashTable) {
		
		return (nashTable[1][1].payoffP1 - nashTable[0][1].payoffP1)
				/(nashTable[0][0].payoffP1 - nashTable[0][1].payoffP1- nashTable[1][0].payoffP1 + nashTable[1][1].payoffP1);
		
	}
	
	public double calculateNEP1(PayOff[][] nashTable) {
		
		return (nashTable[1][1].payoffP2 - nashTable[1][0].payoffP2)
				/(nashTable[0][0].payoffP2 - nashTable[1][0].payoffP2- nashTable[0][1].payoffP2 + nashTable[1][1].payoffP2);
		
	}

}
