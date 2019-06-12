package play;

import java.util.Iterator;
import java.util.List;

import com.winvector.lp.LPException;

import gametree.GameNode;
import gametree.PayOff;
import lp.IteratedDominance;
import play.exception.InvalidStrategyException;

public class G2Strategy extends Strategy {

	
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
		
	}

	@Override
	public void execute() throws InterruptedException {
				
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		
		this.readTree();
		List<String> result = null;
		
		try {
			result = IteratedDominance.iteratedDominance(gameTable);
		} catch (LPException e1) {
			e1.printStackTrace();
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
					if(result.contains(key))
						myStrategy.put(key, 1.0);
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

}
