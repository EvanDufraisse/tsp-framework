package tsp.metaheuristic;

import tsp.Solution;

public class Best {

	private volatile Individu val;
	
	
	public Best() {
		
	}
	
	public synchronized Individu get() {
		return this.val;
	}

	public synchronized void set(Individu ind) {
		if(this.val == null || ind.getLongueur() < this.val.getLongueur()) {
			this.val = ind;
			//System.out.println("val : " + this.val.getLongueur());
		}
	}
	
	
}
