package play;

import java.security.SecureRandom;
import java.util.Iterator;

import gametree.GameNode;
import play.exception.InvalidStrategyException;

public class MyGreedy extends Strategy {

	
	private class Player{
		
		int playerNum = 0;
		
		double pC = 1.0;
		double pD = 0.0;
		
		int moveCounter = 0;
		
		int numAdPC = 0;
		int numMePC = 0;
		
		int seqAdCoop = 0;
		int seqAdDef = 0;
		int seqMeCoop = 0;
		int seqMeDef = 0;
		
		private static final int COP_COP = 1;
		private static final int COP_DEF = 2;
		private static final int DEF_COP = 3;
		private static final int DEF_DEF = 4;
		
		public Player( int number) {
			playerNum = number;
		}
		
		public void updateStats(GameNode lastGame) {
			
			int game = lastGame.getValue();

			game = (playerNum == 2 && game == COP_DEF) ? DEF_COP : (playerNum == 2 && game == DEF_COP) ? COP_DEF : game ;
			
			switch (lastGame.getValue()) {
			case COP_COP:
				
				numMePC ++;
				numAdPC++;
				
				seqMeCoop ++;
				seqMeDef = 0;
				
				seqAdCoop ++;
				seqAdDef = 0;
				
				break;

			case COP_DEF:
				numMePC ++;
				
				seqMeCoop ++;
				seqMeDef = 0;
				
				seqAdCoop = 0;
				seqAdDef ++;
				
				break;
				
			case DEF_COP:
				numAdPC ++;
				
				seqMeDef ++;
				seqMeCoop = 0;
				
				seqAdCoop ++;
				seqAdDef = 0;
				
				break;
				
			case DEF_DEF:
				seqMeDef ++;
				seqMeCoop = 0;
				
				seqAdCoop = 0;
				seqAdDef ++;
			
				break;
			
			default:
				break;
			}
			moveCounter++;
			
		}
		
		//function calculate increase odds of coop
		private double incCoopOdds(double freqC) {
			
			return Math.min(-1 + Math.pow(Math.E,((seqMeDef+1)*freqC/20)), 1.0);
		}
		
		private double decCoopOdds(double freqC) {
			
			return -Math.pow(seqMeCoop, 2)/(100*(freqC + 0.04)) + 1.0;
		}

		public void calculateNextMove() {
			
			if(moveCounter != 0) {
				
				/*double freqC = (double)numAdPC/(double)moveCounter;
				
				//if (opponent coop and we've been defec)
				//OR (we've been coop)
				if((seqAdCoop >=1 && seqMeDef > 1) || (seqMeCoop >= 1)) {		
					//if we coop twice and opponent doesn't ==> don't coop
					//else apply decrease coop function
					pC = (seqMeCoop >= 2) ? Math.min(decCoopOdds(freqC), seqAdCoop): decCoopOdds(freqC);
					pD = 1-pC;
					return;
				}
		
				pC = incCoopOdds(freqC);
				pD = 1-pC;*/
				defect();
			}
		}
		
		
		
		public void defect() {
			pD = 1.0;
			pC = 0.0;
		}
	}
	
	@Override
	public void execute() throws InterruptedException {
		
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		
		Player p1 = new Player(1);
		Player p2 = new Player(2);
		
		while(true) {
		
			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
						
			while(! playComplete ) {
				
				//update stats player 1
				if(myStrategy.getFinalP1Node() != -1) {
					GameNode finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					
					if(finalP1 != null) {
						p1.updateStats(finalP1);					
						System.out.println("Terminal node in last round as P1: " + finalP1);
					}
				}
				
				//update stats player 2
				if(myStrategy.getFinalP2Node() != -1) {
					GameNode finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if(finalP2 != null) {
						p2.updateStats(finalP2);
						System.out.println("Terminal node in last round as P2: " + finalP2);
					}
				}
				
				p1.calculateNextMove();
				p2.calculateNextMove();
				
				Iterator<Integer> iterator = tree.getValidationSet().iterator();
				Iterator<String> keys = myStrategy.keyIterator();
				while(iterator.hasNext()) {
					double[] moves = new double[iterator.next()];
					
					if(iterator.hasNext()) {
						moves[0] = p1.pC;
						moves[1] = p1.pD;
					}else {
						moves[0] = p2.pC;
						moves[1] = p2.pD;
					}
						
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
		System.out.println(p1.numMePC + " VS " + p1.numAdPC);
		System.out.println(p2.numMePC + " VS " + p2.numAdPC);
		
	}

}
