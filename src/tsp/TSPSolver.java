package tsp;

import tsp.heuristic.AHeuristicBasic;
import tsp.metaheuristic.AMetaheuristic;
import tsp.metaheuristic.AMetaheuristicGenetique;
import tsp.metaheuristic.AMetaheuristicGenetiqueParallel;
import tsp.metaheuristic.Best;
import tsp.metaheuristic.Individu;
import tsp.metaheuristic.Sync;

/**
 * 
 * This class is the place where you should enter your code and from which you can create your own objects.
 * 
 * The method you must implement is solve(). This method is called by the programmer after loading the data.
 * 
 * The TSPSolver object is created by the Main class.
 * The other objects that are created in Main can be accessed through the following TSPSolver attributes: 
 * 	- #m_instance :  the Instance object which contains the problem data
 * 	- #m_solution : the Solution object to modify. This object will store the result of the program.
 * 	- #m_timeLimit : the maximum time limit (in seconds) given to the program.
 *  
 * @author Damien Prot, Fabien Lehuede, Axel Grimault
 * @version 2017
 * 
 */
public class TSPSolver {

	// -----------------------------
	// ----- ATTRIBUTS -------------
	// -----------------------------

	/**
	 * The Solution that will be returned by the program.
	 */
	private Solution m_solution;

	/** The Instance of the problem. */
	private Instance m_instance;

	/** Time given to solve the problem. */
	private long m_timeLimit;

	
	// -----------------------------
	// ----- CONSTRUCTOR -----------
	// -----------------------------

	/**
	 * Creates an object of the class Solution for the problem data loaded in Instance
	 * @param instance the instance of the problem
	 * @param timeLimit the time limit in seconds
	 */
	public TSPSolver(Instance instance, long timeLimit) {
		m_instance = instance;
		m_solution = new Solution(m_instance);
		m_timeLimit = timeLimit;
	}

	// -----------------------------
	// ----- METHODS ---------------
	// -----------------------------

	/**
	 * **TODO** Modify this method to solve the problem.
	 * 
	 * Do not print text on the standard output (eg. using `System.out.print()` or `System.out.println()`).
	 * This output is dedicated to the result analyzer that will be used to evaluate your code on multiple instances.
	 * 
	 * You can print using the error output (`System.err.print()` or `System.err.println()`).
	 * 
	 * When your algorithm terminates, make sure the attribute #m_solution in this class points to the solution you want to return.
	 * 
	 * You have to make sure that your algorithm does not take more time than the time limit #m_timeLimit.
	 * 
	 * @throws Exception may return some error, in particular if some vertices index are wrong.
	 */
	public void solve() throws Exception
	{
		m_solution.print(System.err);
		
		// Example of a time loop
		long startTime = System.currentTimeMillis();
		long spentTime = 0;
		//AHeuristicBasic modele = new AHeuristicBasic(this.m_instance, "Basic");
		
		
		
		
		
		/*AMetaheuristicGenetique modele = new AMetaheuristicGenetique(this.m_instance, "Genetique", 20, 100);
		Individu aComparer = new Individu(this.m_instance.getNbCities(), true, this.m_instance);
		Solution s = new Solution(this.m_instance);
		int j = 0;*/
		
		//*Multi-thread
		System.out.println("ok");  
		int cores = Runtime.getRuntime().availableProcessors()-1;
		System.out.println("" + cores);
		int taillePopulationEchange = 6;
		int nbSelections = 10;
		int taillePopulation = 20;
		Best best = new Best();
		
		double[] p_mutation = new double[cores];
		for(int i = 0; i < cores; i++) {
			p_mutation[i] = 0.05 + ((0.2 - 0.05)/cores) * i;
		}
		
		
		
		
		AMetaheuristicGenetiqueParallel[] island = new AMetaheuristicGenetiqueParallel[cores];
		Sync[] stockage = new Sync[cores];
		Thread[] listeThreads = new Thread[cores];
		
		
		
		stockage[0] = new Sync(taillePopulationEchange);
		
		for(int i = 0; i < cores - 1; i++){
		
			stockage[i + 1] = new Sync(taillePopulationEchange);
			island[i] = new AMetaheuristicGenetiqueParallel(this.m_instance, "Modele insulaire", nbSelections, taillePopulation, p_mutation[i], stockage[i], stockage[i+1], best);
			listeThreads[i] = new Thread(island[i]);
			
		}
		System.out.println("ok");
		 island[cores - 1] = new AMetaheuristicGenetiqueParallel(this.m_instance, "Modele insulaire", nbSelections, taillePopulation, p_mutation[cores - 1], stockage[cores - 1], stockage[0], best);
		listeThreads[cores - 1] = new Thread(island[cores - 1]);
		for(int i = 0; i < cores; i++) {
			
			listeThreads[i].start();
			
		}
		 
		 
		do{
			
			/*s = modele.solve(null);
		
			
			if(spentTime%1000 <= 15) {
				//System.out.println(modele.getBest().getLongueur());
				//System.out.println(modele.getLongueur(0) + ", " + modele.getLongueur(1) + ", " + modele.getLongueur(3));
			}
			j++;
			*/
			
			
			//Multi-Thread
			
			
			
			/*if(best.get() != null) {
				this.m_solution = best.get().toSolution();
				//System.out.println("solution : " + best.get().getLongueur());
				//this.m_solution.evaluate();
				//System.out.println("solution :" + this.m_solution.getObjectiveValue());
			}*/
			
		
		
		
			spentTime = System.currentTimeMillis() - startTime;
		}while(spentTime < (m_timeLimit * 1000 - 100) );
		
		
		Solution s = best.get().toSolution();
		

		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			this.m_solution.setCityPosition(i, s.getCity(i));
		}
		//this.m_solution = best.get().toSolution();
		
		/*for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			this.m_solution.setCityPosition(i, island[0].getBest().get(i));
		}*/
		for(int i = 0; i < cores; i++) {
			island[i].arreter();
		}
		
		
		
		
		
		
	}

	// -----------------------------
	// ----- GETTERS / SETTERS -----
	// -----------------------------

	/** @return the problem Solution */
	public Solution getSolution() {
		return m_solution;
	}

	/** @return problem data */
	public Instance getInstance() {
		return m_instance;
	}

	/** @return Time given to solve the problem */
	public long getTimeLimit() {
		return m_timeLimit;
	}

	/**
	 * Initializes the problem solution with a new Solution object (the old one will be deleted).
	 * @param solution : new solution
	 */
	public void setSolution(Solution solution) {
		this.m_solution = solution;
	}

	/**
	 * Sets the problem data
	 * @param instance the Instance object which contains the data.
	 */
	public void setInstance(Instance instance) {
		this.m_instance = instance;
	}

	/**
	 * Sets the time limit (in seconds).
	 * @param time time given to solve the problem
	 */
	public void setTimeLimit(long time) {
		this.m_timeLimit = time;
	}

}
