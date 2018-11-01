package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.Individu;

import java.util.concurrent.ThreadLocalRandom;

//import com.sun.deploy.util.SystemPropertyUtil;

public class AMetaheuristicGenetique extends AMetaheuristic {
	
	private int nbSelections;
	private int taillePopulation;
	private Population population;
	private Population populationSelectionnee;
	private Individu best;
	private int iteration;

	
	public AMetaheuristicGenetique(Instance instance, String name, int nbSelections, int taillePopulation) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.taillePopulation = taillePopulation;
		this.population = new Population(this.taillePopulation, this.m_instance.getNbCities(), this.m_instance);
		this.populationSelectionnee = new Population(this.nbSelections, this.m_instance.getNbCities(), this.m_instance);
		this.best = this.population.get(0);
		this.iteration = 0;		

	}
	
	
	public double getLongueur(int i) throws Exception {
		return this.population.get(i).getLongueur();
	}
	
	public Solution getSolution() throws Exception {
		
		Solution solution = new Solution(this.m_instance);
		
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
	
		long startTime = System.currentTimeMillis();
	
		this.populationSelectionnee = this.population.selectionnerCroiser(this.nbSelections, this.m_instance);
		//this.populationSelectionnee = this.population.selectionAleatoireCroiser(this.nbSelections);
		
		//System.out.println("Selection :" + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.muter(this.m_instance);
		//System.out.println("Mutation : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		//System.out.println("Fusion : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.trier();
		this.population.fusionner(this.populationSelectionnee, this.m_instance);
		
		this.population.selectionner();
		//System.out.println("Tri : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		if(this.iteration%15 == 0) {
			this.population.opt2(this.m_instance);
		}
		
		this.iteration ++;
		if(this.best.getLongueur() > this.population.get(0).getLongueur()) {
			this.best = this.population.get(0);
			
			//this.best.opt_2(m_instance);
		}
		this.population.enleverDoublons();
		
		if(this.iteration%100 == 0) {
			System.out.println(this.population.toString());
			//System.out.println(this.iteration);
			if(this.population.get(2).equals(this.population.get(3))) {
				System.out.println("erreur !");
			}
		}
		
		//System.out.println("Best : " + (System.currentTimeMillis()-startTime));
		
		/*Individu ind = new Individu(this.m_instance.getNbCities(), this.m_instance);
		ind.opt_2(this.m_instance);
		if(ind.getLongueur() < this.getBest().getLongueur()) {
			this.best = ind;
		}*/

		return this.getSolution();
	}

}
