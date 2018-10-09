package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.Individu;

import java.util.concurrent.ThreadLocalRandom;

public class AMetaheuristicGenetique extends AMetaheuristic {
	
	private int nbSelections;
	private double pMutation;
	private Population population;
	private Population populationSelectionnee;
	
	public AMetaheuristicGenetique(Instance instance, String name) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.pMutation = pMutation;
		this.population = new Population(100, this.m_instance.getNbCities());
		this.populationSelectionnee = new Population(50, this.m_instance.getNbCities());
	}
	
	
	
	
	public Solution getSolution() throws Exception {
		Solution solution = new Solution(this.m_instance);
		Individu best = this.population.get(0);
		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, best.get(i));
		}
		solution.setCityPosition(0, this.m_instance.getNbCities());
		return solution;
	}
	
		
	
	
	
	
	

	@Override
	public Solution solve(Solution sol) throws Exception {
		// TODO Auto-generated method stub
		
		this.populationSelectionnee = this.population.selectionnerCroiser(50, this.m_instance);
		this.population.fusionner(this.populationSelectionnee);
		this.population.trier(m_instance);
		return this.getSolution();
	}

}
