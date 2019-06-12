package play;

import java.security.SecureRandom;
import java.util.Iterator;

import gametree.GameNode;
import play.exception.InvalidStrategyException;

public class T1MyStrategy extends Strategy {

	
	//Classe Player
	private class Player{
		
		//nr do player (1 ou 2)
		int playerNum = 0;
		
		//probabilidade de cooperar e probabilidade de defect, respetivamente
		double pC = 1.0;
		double pD = 0.0;
		
		//nr de jogadas
		int moveCounter = 0;
		
		//nr total de cooperaçoes do oponente
		int numAdPC = 0;
		
		//nr de cooperates do adversário consecutivos
		int seqAdCoop = 0;
		
		//nr de cooperates e de defects, nossos, consecutivos
		int seqMeCoop = 0;
		int seqMeDef = 0;
		
		private static final int COP_COP = 1;
		private static final int COP_DEF = 2;
		private static final int DEF_COP = 3;
		private static final int DEF_DEF = 4;
		
		public Player( int number) {
			playerNum = number;
		}
		
		//atualiza as estatisticas do último jogo
		public void updateStats(GameNode lastGame) {
			
			int game = lastGame.getValue();

			//troca o DEF_COP por COP_DEF, caso seja o jogador 2
			game = (playerNum == 2 && game == COP_DEF) ? DEF_COP : (playerNum == 2 && game == DEF_COP) ? COP_DEF : game ;
			
			switch (game) {
			case COP_COP:
				
				numAdPC++;
				
				seqMeCoop ++;
				seqMeDef = 0;
				
				seqAdCoop ++;
				
				break;

			case COP_DEF:
				
				seqMeCoop ++;
				seqMeDef = 0;
				
				seqAdCoop = 0;
				
				break;
				
			case DEF_COP:
				
				numAdPC ++;
				
				seqMeDef ++;
				seqMeCoop = 0;
				
				seqAdCoop ++;
				
				break;
				
			case DEF_DEF:

				seqMeDef ++;
				seqMeCoop = 0;
				
				seqAdCoop = 0;
			
				break;
			
			default:
				break;
			}
			moveCounter++;
			
		}
		
		//funcao que calcula o aumento da probabilidade de cooperar
		private double incCoopOdds(double freqC) {
			
			return Math.min(-1 + Math.pow(Math.E,((seqMeDef+1)*(freqC*(seqAdCoop+1))/60)), 1.0);
		}
		
		//funcao que calcula o decrescimo da probabilidade de cooperar
		private double decCoopOdds(double freqC) {
			
			return -Math.pow(seqMeCoop, 2)/(100*(freqC + 0.04)) + 1.0;
		}

		//calcula proxima jogada
		public void calculateNextMove() {
			
			if(moveCounter != 0) {
				
				//frequencia de cooperates do oponente
				double freqC = (double)numAdPC/(double)moveCounter;
				
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
				pD = 1-pC;
			}
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
		
	}

}
