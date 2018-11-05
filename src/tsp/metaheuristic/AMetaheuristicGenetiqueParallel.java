package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;

// TODO: Auto-generated Javadoc
/**
 * The Class AMetaheuristicGenetiqueParallel.
 */
public class AMetaheuristicGenetiqueParallel extends AMetaheuristic implements Runnable{


	
	/** Le nombre d'individu qui seront croisés à chaque itération */
	private int nbSelections;
	
	/** La taille de la population */
	private int taillePopulation;
	
	/** The population. */
	private Population population;
	
	/** La population qui recense les individus à croiser. */
	private Population populationSelectionnee;
	
	/** Le meilleur individu que l'on a trouvé */
	private Individu best;
	
	/** Le numéro de l'itération de l'algorithme génétique */
	private int iteration;
	
	/** The running. */
	private volatile boolean running;
	
	/** L'objet qui sert à stocker les individus à immigrer dans la population, et provenant d'une autre île, et ici d'un autre thread. */
	private Sync syncFrom;
	
	/** L'objet qui sert à stocker les individus à émigrer dans une autre île, ici située dans un autre thread. */
	private Sync syncTo;
	
	/** L'objet qui sert à stocker le meilleur individu trouvé, en prenant en compte toutes les îles. */
	private Best meilleur;
	
	/** La probabilité de mutation d'un individu. */
	private double p_mutation;

	
	/**
	 * Instantiates a new a metaheuristic genetique parallel.
	 *
	 * @param instance the instance
	 * @param name the name
	 * @param nbSelections Le nombre de selections
	 * @param taillePopulation La taille de la population
	 * @param p_mutation La probabilité de mutation
	 * @param syncFrom
	 * @param syncTo
	 * @param meilleur
	 * @throws Exception
	 */
	public AMetaheuristicGenetiqueParallel(Instance instance, String name, int nbSelections, int taillePopulation, double p_mutation, Sync syncFrom, Sync syncTo, Best meilleur) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.taillePopulation = taillePopulation;
		this.population = new Population(this.taillePopulation, this.m_instance);
		this.populationSelectionnee = new Population(this.nbSelections, this.m_instance);
		this.best = this.population.get(0);
		this.iteration = 0;		
		this.running = true;
		this.syncFrom = syncFrom;
		this.syncTo = syncTo;
		this.meilleur = meilleur;
		this.p_mutation = p_mutation;

	}
	
	/* La méthode appelée au démarrage d'un thread.
	 * 
	 */
	@Override
	public void run() {
	
		while(running) {
			try {
				this.solve(null);
			} catch (Exception e) {
			
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Arrete le thread.
	 */
	public void arreter() {
		this.running = false;
	}
	
	/**
	 *
	 *
	 * @param i la place de l'individu dans la population.
	 * @return La longueur du chemin de l'individu i.
	 * @throws Exception
	 */
	public double getLongueur(int i) throws Exception {
		return this.population.get(i).getLongueur();
	}
	
	/**
	 * .
	 *
	 * @return Un objet Solution correspondant au meilleur individu troucé sur cette île.
	 * @throws Exception
	 */
	public Solution getSolution() throws Exception {
		
		Solution solution = new Solution(this.m_instance);
		
		for(int i = 0; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, this.best.get(i));
		}
		return solution;
	}
	
	/**
	 * 
	 * @return La population de l'île.
	 */
	public Population getPopulation() {
		return this.population;
	}
	
		 
	/**
	 * 
	 *
	 * @return Le meilleur individu trouvé sur l'île.
	 */
	public synchronized Individu getBest() {
		return this.best;
	}
	

	/* 
	 * Produit une itération de l'algorithme génétique.
	 */
	@Override
	public Solution solve(Solution sol) throws Exception {
	
		this.populationSelectionnee = this.population.selectionnerCroiser(this.nbSelections, this.m_instance);
		
	
		this.population.muter(this.p_mutation);
		

		this.population.trier();
		this.populationSelectionnee.trier();
		this.population.fusionner(this.populationSelectionnee);
		
	
		if(this.iteration%15 == 0) {
			this.population.opt2();
		}
		
		this.iteration ++;
		
		if(this.best.getLongueur() > this.population.get(0).getLongueur()) {
			this.best = this.population.get(0);
			this.meilleur.set(this.best);
			
		}
		this.population.enleverDoublons();
	
		if(this.iteration%100 == 0) {
			//System.out.println("iteration " + this.iteration + " : " + this.population.toString());
			
			
		}
		if(this.iteration%10 == 0) {
			this.migrer();
			this.recuperer();
		}
		
		
		return this.getSolution();
	}

	/**
	 * Migre un nombre d'individu égal à la taille du tableau stocké par syncTo. On donne les meilleurs individus.
	 */
	public void migrer() {
		
		Individu[] aMigrer = new Individu[this.syncTo.getSize()];
		
		for(int i = 0; i < this.syncTo.getSize(); i++) {
			aMigrer[i] = this.population.get(i);
		}
		
		try {
			this.syncTo.put(aMigrer);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}
	
	/**
	 * Récupère un nombre d'individus égal à la taille du tableau stocké par syncFrom. On prend les meilleurs individus.
	 *
	 * @throws Exception
	 */
	public void recuperer() throws Exception {
		
		
			Individu[] recu = this.syncFrom.get();
	if(recu[0] != null) {
		
		for(int i = 0; i < this.syncFrom.getSize(); i++) {
			this.population.inserer(recu[i]);
		}
		
	}
	}

}
