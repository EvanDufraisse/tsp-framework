package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.Individu;

import java.util.concurrent.ThreadLocalRandom;

//import com.sun.deploy.util.SystemPropertyUtil;

public class AMetaheuristicGenetique extends AMetaheuristic {
	
	private int nbSelections;
	private int taillePopulation;
	private double pMutation;
	private Population population;
	private Population populationSelectionnee;
	private Individu best;
	
	public AMetaheuristicGenetique(Instance instance, String name, int nbSelections, int taillePopulation) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.taillePopulation = taillePopulation;
		this.pMutation = pMutation;
		this.population = new Population(this.taillePopulation, this.m_instance.getNbCities(), this.m_instance);
		//System.out.println("Taille = " + this.population.get(0).getIndividu().length);
		this.populationSelectionnee = new Population(this.nbSelections, this.m_instance.getNbCities(), this.m_instance);
		this.best = this.population.get(0);
		//System.out.println("Taille = " + this.populationSelectionnee.get(0).getIndividu().length);

		
	}
	
	
	public double getLongueur(int i) throws Exception {
		return this.population.get(i).getLongueur();
	}
	
	public Solution getSolution() throws Exception {
		Solution solution = new Solution(this.m_instance);
		
		//Individu best = this.population.get(0);
		//System.out.println("length = " + best.getIndividu().length);
		//System.out.println("198 : " + best.get(198));
		//System.out.println("Max = " + best.getMax());
		//System.out.println(best.getIndividu()[197]);
		//this.best.opt_2(this.m_instance);
		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, this.best.get(i));
		}
		
		return solution;
	}
	
		
	public Individu getBest() {
		return this.best;
	}
	

	@Override
	public Solution solve(Solution sol) throws Exception {
		// TODO Auto-generated method stub
		//System.out.println(this.population.get().size());
		long startTime = System.currentTimeMillis();
	
		this.populationSelectionnee = this.population.selectionnerCroiser(this.nbSelections, this.m_instance);
		//System.out.println("Selection :" + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.muter(this.m_instance);
		//System.out.println("Mutation : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.fusionner(this.populationSelectionnee, m_instance);
		//System.out.println("Fusion : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.trier();
		this.population.selectionner();
		//System.out.println("Tri : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		if(this.best.getLongueur() > this.population.get(0).getLongueur()) {
			this.best = this.population.get(0);
			//this.best.opt_2(m_instance);
		}
		//System.out.println("Best : " + (System.currentTimeMillis()-startTime));

		return this.getSolution();
	}

}
