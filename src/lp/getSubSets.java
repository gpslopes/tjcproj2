package lp;

import java.util.ArrayList;
import java.util.List;

public class getSubSets {

	public static List<boolean[]> getSubsets(int j, int k, int n) {
		boolean[] b = new boolean[n];
		List<boolean[]> subset = new ArrayList<boolean[]>();
		if (k==0) {
			for(int i=0;i<b.length;i++) b[i]=false;
			subset.add(b);
		}
		else 
			if (n-j==k) {
				for(int i=0;i<b.length;i++) b[i]=true;
				subset.add(b);
			}
			else {
				List<boolean[]> s1=getSubsets(j+1,k-1,n);
				for(int i=0;i<s1.size();i++) {
					b = s1.get(i);
					b[j]=true;
					subset.add(b);
				}
				List<boolean[]> s0=getSubsets(j+1,k,n);
				for(int i=0;i<s0.size();i++) {
					b = s0.get(i);
					b[j]=false;
					subset.add(b);
				}
			}
			return subset;
	}

	public static void showSubSet(List<boolean[]> s) {
		int n = s.get(0).length;
		boolean[] b = new boolean[n];
		for(int i=0;i<s.size();i++) {
			b = s.get(i);
			System.out.print("{");
			for(int j=0;j<n;j++) 
				if (b[j]) System.out.print(" " + 1);
				else System.out.print(" " + 0);
				System.out.println(" }");
			}
		}

	public static void main(String[] args) {
//		List<boolean[]> s1=getSubsets(0,2,8);
//		showSubSet(s1);

		showSubSet(getSubsets(1,2,5));
		System.out.println("DIVISION");
		showSubSet(getSubsets(0,2,5));
	}

}
