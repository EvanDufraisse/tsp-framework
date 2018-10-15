package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.Individu;

import java.util.concurrent.ThreadLocalRandom;

import com.sun.deploy.util.SystemPropertyUtil;

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
		//System.out.println("Taille = " + this.population.get(0).getIndividu().length);
		this.populationSelectionnee = new Population(50, this.m_instance.getNbCities());
		//System.out.println("Taille = " + this.populationSelectionnee.get(0).getIndividu().length);

		
	}
	
	
	
	
	public Solution getSolution() throws Exception {
		Solution solution = new Solution(this.m_instance);
		Individu best = this.population.get(0);
		//System.out.println("length = " + best.getIndividu().length);
		//System.out.println("198 : " + best.get(198));
		//System.out.println("Max = " + best.getMax());
		//System.out.println(best.getIndividu()[197]);
		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, best.get(i));
		}
		
		return solution;
	}
	
		
	
	
	
	
	

	@Override
	public Solution solve(Solution sol) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(this.population.get().size());
		this.populationSelectionnee = this.population.selectionnerCroiser(50, this.m_instance);
		this.population.fusionner(this.populationSelectionnee);
		this.population.trier(m_instance);
		return this.getSolution();
	}

}
