package play;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gametree.GameNode;
import gametree.PayOff;
import play.exception.InvalidStrategyException;

public class G7Strategy extends Strategy {

	private PayOff[][] gameTable = null;

	private PayOff[][] readTree() {

		List<Integer> list = this.tree.getValidationSet();
		gameTable = new PayOff[list.get(0)][list.get(1)];

		for (Integer integer : list) {
			System.out.println("List: " + integer);
		}

		GameNode root = this.tree.getRootNode();
		System.out.println("root: " + root.getLabel());
		Iterator<GameNode> itP1 = root.getChildren();
		int countP1Moves = 0;
		int countP2Moves = 0;

		while (itP1.hasNext()) {

			GameNode p1Node = itP1.next();
			System.out.println("child label: " + p1Node.getLabel());
			Iterator<GameNode> itP2 = p1Node.getChildren();

			while (itP2.hasNext()) {
				GameNode p2Node = itP2.next();

				gameTable[countP1Moves][countP2Moves] = new PayOff(p1Node.getLabel(), p2Node.getLabel(),
						p2Node.getPayoffP1(), p2Node.getPayoffP2());

				System.out.println("leaf label:" + p2Node.getOutcome());
				countP2Moves++;

			}

			countP2Moves = 0;
			countP1Moves++;
		}
		System.out.println("END");

		return gameTable;
	}

	public List<Integer> maxMinP2() {

		double[] maxMinPayoff = new double[gameTable[0].length];

		for (int i = 0; i < maxMinPayoff.length; i++) {
			maxMinPayoff[i] = Double.MAX_VALUE;
		}

		for (int j = 0; j < gameTable[0].length; j++) {
			for (int i = 0; i < gameTable.length; i++) {

				if (gameTable[i][j].payoffP2 < maxMinPayoff[j]) {
					maxMinPayoff[j] = gameTable[i][j].payoffP2;
				}
			}
		}

		List<Integer> posMaxMin = new ArrayList<Integer>();
		double maxMinValue = -Double.MAX_VALUE;
		for (int x = 0; x < maxMinPayoff.length; x++) {
			if (maxMinValue < maxMinPayoff[x]) {
				maxMinValue = maxMinPayoff[x];
				posMaxMin.clear();
				posMaxMin.add(x);
			} else if (maxMinValue == maxMinPayoff[x]) {
				posMaxMin.add(x);
			}
		}

		System.out.println(gameTable[0][posMaxMin.get(0)].p2Move);
		return posMaxMin;
	}

	public List<Integer> maxMinP1() {

		double[] maxMinPayoff = new double[gameTable.length];

		for (int i = 0; i < maxMinPayoff.length; i++) {
			maxMinPayoff[i] = Double.MAX_VALUE;
		}

		for (int i = 0; i < gameTable.length; i++) {
			for (int j = 0; j < gameTable[0].length; j++) {
				if (gameTable[i][j].payoffP1 < maxMinPayoff[i]) {
					maxMinPayoff[i] = gameTable[i][j].payoffP1;
				}
			}
		}

		List<Integer> posMaxMin = new ArrayList<Integer>();
		double maxMinValue = -Double.MAX_VALUE;
		for (int x = 0; x < maxMinPayoff.length; x++) {
			if (maxMinValue < maxMinPayoff[x]) {
				maxMinValue = maxMinPayoff[x];
				posMaxMin.clear();
				posMaxMin.add(x);
			} else if (maxMinValue == maxMinPayoff[x]) {
				posMaxMin.add(x);
			}
		}
		System.out.println(gameTable[posMaxMin.get(0)][0].p1Move);
		return posMaxMin;
	}

	public List<Integer> minMaxP1() {

		double[] minMaxPayoff = new double[gameTable[0].length];

		for (int i = 0; i < minMaxPayoff.length; i++) {
			minMaxPayoff[i] = Double.MIN_VALUE;
		}

		for (int j = 0; j < gameTable[0].length; j++) {
			for (int i = 0; i < gameTable.length; i++) {
				if (gameTable[i][j].payoffP1 > minMaxPayoff[i]) {
					minMaxPayoff[j] = gameTable[i][j].payoffP1;
				}
			}
		}

		List<Integer> posMaxMin = new ArrayList<Integer>();
		double maxMinValue = Double.MAX_VALUE;
		for (int x = 0; x < minMaxPayoff.length; x++) {
			if (maxMinValue > minMaxPayoff[x]) {
				maxMinValue = minMaxPayoff[x];
				posMaxMin.clear();
				posMaxMin.add(x);
			} else if (maxMinValue == minMaxPayoff[x]) {
				posMaxMin.add(x);
			}
		}
		System.out.println("Min Max P1 -> P2 plays: " + gameTable[0][posMaxMin.get(0)].p2Move);
		return posMaxMin;
	}

	public List<Integer> minMaxP2() {

		double[] minMaxPayoff = new double[gameTable.length];

		for (int i = 0; i < minMaxPayoff.length; i++) {
			minMaxPayoff[i] = Double.MIN_VALUE;
		}

		for (int i = 0; i < gameTable.length; i++) {
			for (int j = 0; j < gameTable[0].length; j++) {
				if (gameTable[i][j].payoffP1 > minMaxPayoff[i]) {
					minMaxPayoff[i] = gameTable[i][j].payoffP2;
				}
			}
		}

		List<Integer> posMaxMin = new ArrayList<Integer>();
		double maxMinValue = Double.MAX_VALUE;
		for (int x = 0; x < minMaxPayoff.length; x++) {
			if (maxMinValue > minMaxPayoff[x]) {
				maxMinValue = minMaxPayoff[x];
				posMaxMin.clear();
				posMaxMin.add(x);
			} else if (maxMinValue == minMaxPayoff[x]) {
				posMaxMin.add(x);
			}
		}
		System.out.println("Min Max P2 -> P1 plays: " + gameTable[0][posMaxMin.get(0)].p2Move);
		return posMaxMin;
	}

	public String calculateSolForMaxMinP2(List<Integer> maxMin) {

		int res = -1;

		double payoff = -Double.MAX_VALUE;
		if (maxMin.size() == 1) {
			int colPos = maxMin.get(0);
			for (int i = 0; i < gameTable.length; i++) {
				if (payoff < gameTable[i][colPos].payoffP1) {

					res = i;
					payoff = gameTable[i][colPos].payoffP1;
				}
			}
			return gameTable[res][0].p1Move;
		} else {

			double bestPayoff = -Double.MAX_VALUE;
			for (int i = 0; i < gameTable.length; i++) {
				double sumPayoffs = -Double.MAX_VALUE;
				for (Integer pos : maxMin) {
					sumPayoffs += gameTable[i][pos].payoffP1;
				}
				if (bestPayoff < sumPayoffs) {
					bestPayoff = sumPayoffs;
					res = i;
				}
			}
			return gameTable[res][0].p1Move;
		}
	}

	public String calculateSolForMaxMinP1(List<Integer> maxMin) {

		int res = -1;

		double payoff = -Double.MAX_VALUE;
		if (maxMin.size() == 1) {
			int rowPos = maxMin.get(0);
			for (int j = 0; j < gameTable[0].length; j++) {
				if (payoff < gameTable[rowPos][j].payoffP2) {
					res = j;
					payoff = gameTable[rowPos][j].payoffP2;
				}

			}
			return gameTable[0][res].p2Move;
		} else {

			double bestPayoff = -Double.MAX_VALUE;
			for (int j = 0; j < gameTable[0].length; j++) {
				double sumPayoffs = -Double.MAX_VALUE;
				for (Integer pos : maxMin) {
					sumPayoffs += gameTable[pos][j].payoffP2;
				}
				if (bestPayoff < sumPayoffs) {
					bestPayoff = sumPayoffs;
					res = j;
				}
			}
			return gameTable[0][res].p2Move;
		}
	}

	@Override
	public void execute() throws InterruptedException {

		while (!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}

		while (true) {

			PlayStrategy myStrategy = this.getStrategyRequest();
			if (myStrategy == null) // Game was terminated by an outside event
				break;
			boolean playComplete = false;

			this.readTree();
			while (!playComplete) {
				if (myStrategy.getFinalP1Node() != -1) {
					GameNode finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					if (finalP1 != null)
						System.out.println("Terminal node in last round as P1: " + finalP1);
				}

				if (myStrategy.getFinalP2Node() != -1) {
					GameNode finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					if (finalP2 != null)
						System.out.println("Terminal node in last round as P2: " + finalP2);
				}

				Iterator<String> keys = myStrategy.keyIterator();
				String p1Play = calculateSolForMaxMinP2(maxMinP2());
				String p2Play = calculateSolForMaxMinP1(maxMinP1());
				minMaxP1();
				minMaxP2();

				System.out.println("P1Play " + p1Play);
				System.out.println("P2Play " + p2Play);
				while (keys.hasNext()) {
					String key = keys.next();
					if (key.equals(p1Play) || key.equals(p2Play))
						myStrategy.put(key, 1.0);
					else
						myStrategy.put(key, 0.0);
				}

				try {
					this.provideStrategy(myStrategy);
					playComplete = true;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());
					;
					e.printStackTrace(System.err);
				}
			}
		}

	}

}
