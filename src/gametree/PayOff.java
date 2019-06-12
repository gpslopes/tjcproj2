package gametree;

public class PayOff{
	
	public String p1Move;
	public String p2Move;
	public double payoffP1;
	public double payoffP2;
	
	public PayOff(String p1Move, String p2Move, double u1, double u2) {
		this.p1Move = p1Move;
		this.p2Move = p2Move;
		this.payoffP1 = u1;
		this.payoffP2 = u2;
	}
}