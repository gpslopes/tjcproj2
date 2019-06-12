package play;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.List;

import com.winvector.lp.LPException;

import gametree.GameNode;
import gametree.PayOff;
import lp.IteratedDominance;
import play.exception.InvalidStrategyException;

public class SomeStrategy extends Strategy {

	
	private PayOff[][] gameTable = null;
	
		private void readTree() {
	
		List<Integer> list = this.tree.getValidationSet();
		gameTable = new PayOff[list.get(0)][list.get(1)];
		
		for (Integer integer : list) {
			System.out.println("List: " + integer);
		}
		System.out.println("HERE");
		GameNode root = this.tree.getRootNode();
		System.out.println("root: " +root.getLabel());
		Iterator<GameNode> itP1 = root.getChildren();
		int countP1Moves = 0;
		int countP2Moves = 0;
		
		while (itP1.hasNext()) {
			
			GameNode p1Node = itP1.next();
			System.out.println("child label: " + p1Node.getLabel());
			Iterator<GameNode> itP2 = p1Node.getChildren();
			
			while (itP2.hasNext()) {
				GameNode p2Node = itP2.next();
				
				gameTable[countP1Moves][countP2Moves] = new PayOff(p1Node.getLabel(), p2Node.getLabel(), p2Node.getPayoffP1(), p2Node.getPayoffP2());

				System.out.println("leaf label:" + p2Node.getOutcome());
				countP2Moves++;
				
			}
			
			countP2Moves = 0;
			countP1Moves++;
		}
		System.out.println("END");
		
	}

	@Override
	public void execute() throws InterruptedException {
		
		SecureRandom random = new SecureRandom();
		
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		
		while(true) {
		
			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
						
			this.readTree();
			
			System.out.println("Start ID");
			
			boolean[][] result;
			
			try {
				result = IteratedDominance.iteratedDominance(gameTable);
			} catch (LPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Finish ID");
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
				
				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();
				
				while(iterator.hasNext()) {
					double[] moves = new double[iterator.next()];
					double sum = 0;
					for(int i = 0; i < moves.length - 1; i++) {
						moves[i] = random.nextDouble();
						while(sum + moves[i] >= 1) moves[i] = random.nextDouble();
						sum = sum + moves[i];
					}
					moves[moves.length-1] = ((double) 1) - sum;
					
					for(int i = 0; i < moves.length; i++) {
						if(!keys.hasNext()) {
							System.err.println("PANIC: Strategy structure does not match the game.");
							return;
						}
						myStrategy.put(keys.next(), moves[i]);
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
