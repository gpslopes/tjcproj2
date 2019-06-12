package gametree;

import java.util.ArrayList;
import java.util.List;

public class NormalFormProblem {

	public List<String> rowMoves;
	public List<String> colMoves;
	public boolean[] pRow, pCol;
	public double[][] u1, u2;
	
	
	public NormalFormProblem() {
	}

	public NormalFormProblem(List<String> rowMoves, List<String> colMoves, boolean[] pRow, boolean[] pCol,
			double[][] u1, double[][] u2) {
		this.rowMoves = rowMoves;
		this.colMoves = colMoves;
		this.pRow = pRow;
		this.pCol = pCol;
		this.u1 = u1;
		this.u2 = u2;
	}
	
	public List<String> getSelectedRows() {
		
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < pRow.length; i++) 
			if(pRow[i]) 
				list.add(rowMoves.get(i));
			
		
		return list;
	}
	
	public List<String> getSelectedCols() {
		
		List<String> list = new ArrayList<String>();
		
		for (int i = 0; i < pCol.length; i++) 
			if(pCol[i]) 
				list.add(colMoves.get(i));
			
		
		return list;
	}
	
}
