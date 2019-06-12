package play;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;

/**********************************************************************************
 * This strategy implements a modified version of the well-known tit-for-tat. 
 * The first move is random, and all subsequent ones simply mimic the opponent's 
 * previous move.
 *
 **********************************************************************************/
public class G1Strategy extends Strategy {

	private List<GameNode> getReversePath(GameNode current) {		
		try {
			GameNode n = current.getAncestor();
			List<GameNode> l =  getReversePath(n);
			l.add(current);
			return l;
		} catch (GameNodeDoesNotExistException e) {
			List<GameNode> l = new ArrayList<GameNode>();
			l.add(current);
			return l;
		}
	}
	
	private void cumputeStrategy(List<GameNode> listP1, 
			List<GameNode> listP2,
			PlayStrategy myStrategy,
			SecureRandom random) throws GameNodeDoesNotExistException {
	
		Set<String> oponentMoves = new HashSet<String>();
		
		//When we played as Player1 we are going to check what were the moves
		//of our opponent as player2.
		for(GameNode n: listP1) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer2()) {
				oponentMoves.add(n.getLabel());
			}
		}
		
		//When we played as Player2 we are going to check what were the moves
		//of our opponent as player1.
		for(GameNode n: listP2) {
			if(n.isNature() || n.isRoot()) continue;
			if(n.getAncestor().isPlayer1()) {
				oponentMoves.add(n.getLabel());
			}
		}
		
		
		//We now set our strategy to have a probability of 1.0 for the moves used
		//by our adversary in the previous round and zero for the remaining ones.
		Iterator<String> moves = myStrategy.keyIterator();
		
		
		
//		if (myStrategy.getMaximumNumberOfIterations() <= 1 ) {
//			// We now set our strategy to have a probability of 1.0 for the moves used
//			// by our adversary in the previous round and zero for the remaining ones.
//			while (moves.hasNext()) {
//				String k = moves.next();
//				if (k.toLowerCase().contains("def")) {
//					myStrategy.put(k, new Double(1));
//					System.err.println("Setting " + k + " to prob 1.0");
//				} else {
//					myStrategy.put(k, new Double(0));
//					System.err.println("Setting " + k + " to prob 0.0");
//				}
//
//			}
//			return;
//		}
		
		if (myStrategy.isFirstRound()) {
			System.out.println("FIRST ROUND");
			// We now set our strategy to have a probability of 1.0 for the moves used
			// by our adversary in the previous round and zero for the remaining ones.
			while (moves.hasNext()) {
				String k = moves.next();
				if (k.toLowerCase().contains("coop")) {
					myStrategy.put(k, new Double(1));
					System.out.println("FIRST COOP");
					System.err.println("Setting " + k + " to prob 1.0");
				} else {
					myStrategy.put(k, new Double(0));
					System.err.println("Setting " + k + " to prob 0.0");
				}
			}
			return;
		}
		if(myStrategy.probabilityForNextIteration() < (double)1/3) {
			while (moves.hasNext()) {
				String k = moves.next();
				if (k.toLowerCase().contains("def")) {
					myStrategy.put(k, new Double(1));
					System.err.println("Setting " + k + " to prob 1.0");
				} else {
					myStrategy.put(k, new Double(0));
					System.err.println("Setting " + k + " to prob 0.0");
				}
			}
			return;
		}
		
		while(moves.hasNext()) {
			String k = moves.next();
			if(oponentMoves.contains(k)) {
				myStrategy.put(k, new Double(1));
				System.err.println("Setting " + k + " to prob 1.0");
			} else {
				myStrategy.put(k, new Double(0));
				System.err.println("Setting " + k + " to prob 0.0");
			}
				
		}					
	}
	
	@Override
	public void execute() throws InterruptedException {

		SecureRandom random = new SecureRandom();

		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}

		GameNode finalP1 = null;
		GameNode finalP2 = null;
				
		while(true) {

			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
			
			while(! playComplete ) {
				if(myStrategy.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if(finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}

				if(myStrategy.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}

				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();
				
				if (myStrategy.isFirstRound()) {
					System.out.println("FIRST ROUND");
					// We now set our strategy to have a probability of 1.0 for the moves used
					// by our adversary in the previous round and zero for the remaining ones.
					while (keys.hasNext()) {
						String k = keys.next();
						if (k.toLowerCase().contains("coop")) {
							myStrategy.put(k, new Double(1));
							System.out.println("FIRST COOP");
							System.err.println("Setting " + k + " to prob 1.0");
						} else {
							myStrategy.put(k, new Double(0));
							System.err.println("Setting " + k + " to prob 0.0");
						}
					}
				} else {
					//Lets mimic our adversary strategy (at least what we can infer)
					List<GameNode> listP1 = getReversePath(finalP1);
					List<GameNode> listP2 = getReversePath(finalP2);
					
					try { cumputeStrategy(listP1, listP2, myStrategy, random); }
					catch( GameNodeDoesNotExistException e ) {
						System.err.println("PANIC: Strategy structure does not match the game.");
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
