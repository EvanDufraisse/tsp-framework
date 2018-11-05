package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;
import tsp.metaheuristic.Individu;

import java.util.concurrent.ThreadLocalRandom;

// TODO: Auto-generated Javadoc
//import com.sun.deploy.util.SystemPropertyUtil;

/**
 * The Class AMetaheuristicGenetique.
 */
public class AMetaheuristicGenetique extends AMetaheuristic {
	
	/** The nb selections. */
	private int nbSelections;
	
	/** The taille population. */
	private int taillePopulation;
	
	/** The population. */
	private Population population;
	
	/** The population selectionnee. */
	private Population populationSelectionnee;
	
	/** The best. */
	private Individu best;
	
	/** The iteration. */
	private int iteration;
	
	/** The p mutation. */
	private double p_mutation;

	
	/**
	 * Instantiates a new a metaheuristic genetique.
	 *
	 * @param instance the instance
	 * @param name the name
	 * @param nbSelections the nb selections
	 * @param taillePopulation the taille population
	 * @param p_mutation the p mutation
	 * @throws Exception the exception
	 */
	public AMetaheuristicGenetique(Instance instance, String name, int nbSelections, int taillePopulation, double p_mutation) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.taillePopulation = taillePopulation;
		this.population = new Population(this.taillePopulation, this.m_instance);
		this.populationSelectionnee = new Population(this.nbSelections, this.m_instance);
		this.best = this.population.get(0);
		this.iteration = 0;		
		this.p_mutation = p_mutation;
	}
	
	
	/**
	 * Gets the longueur of individual i.
	 *
	 * @param i the i
	 * @return the longueur
	 * @throws Exception the exception
	 */
	public double getLongueur(int i) throws Exception {
		return this.population.get(i).getLongueur();
	}
	
	/**
	 * Gets the solution.
	 *
	 * @return the solution
	 * @throws Exception the exception
	 */
	public Solution getSolution() throws Exception {
		
		Solution solution = new Solution(this.m_instance);
		
		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, this.best.get(i));
		}
		return solution;
	}
	
		
	/**
	 * Gets the best.
	 *
	 * @return the best
	 */
	public Individu getBest() {
		return this.best;
	}
	

	/* (non-Javadoc)
	 * @see tsp.metaheuristic.AMetaheuristic#solve(tsp.Solution)
	 */
	@Override
	public Solution solve(Solution sol) throws Exception {
		// TODO Auto-generated method stub
	
		long startTime = System.currentTimeMillis();
	
		this.populationSelectionnee = this.population.selectionnerCroiser(this.nbSelections, this.m_instance);
		//this.populationSelectionnee = this.population.selectionAleatoireCroiser(this.nbSelections);
		
		//System.out.println("Selection :" + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.muter(this.p_mutation);
		//System.out.println("Mutation : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		
		//System.out.println("Fusion : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		this.population.trier();
		this.populationSelectionnee.trier();
		this.population.fusionner(this.populationSelectionnee);
		
	//	this.population.selectionner();
		//System.out.println("Tri : " + (System.currentTimeMillis()-startTime));
		startTime = System.currentTimeMillis();
		if(this.iteration%15 == 0) {
			this.population.opt2();
		}
		
		this.iteration ++;
		if(this.best.getLongueur() > this.population.get(0).getLongueur()) {
			this.best = this.population.get(0);
			
			
		}
		this.population.enleverDoublons();
	
		if(this.iteration%10 == 0) {
			System.out.println(this.population.toString());

			
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
