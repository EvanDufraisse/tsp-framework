package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;

public class AMetaheuristicGenetiqueParallel extends AMetaheuristic implements Runnable{


	
	private int nbSelections;
	private int taillePopulation;
	private Population population;
	private Population populationSelectionnee;
	private Individu best;
	private int iteration;
	private volatile boolean running;
	private Sync syncFrom;
	private Sync syncTo;
	private Best meilleur;
	private double p_mutation;

	
	public AMetaheuristicGenetiqueParallel(Instance instance, String name, int nbSelections, int taillePopulation, double p_mutation, Sync syncFrom, Sync syncTo, Best meilleur) throws Exception {
		super(instance, name);
		this.nbSelections = nbSelections;
		this.taillePopulation = taillePopulation;
		this.population = new Population(this.taillePopulation, this.m_instance.getNbCities(), this.m_instance);
		this.populationSelectionnee = new Population(this.nbSelections, this.m_instance.getNbCities(), this.m_instance);
		this.best = this.population.get(0);
		this.iteration = 0;		
		this.running = true;
		this.syncFrom = syncFrom;
		this.syncTo = syncTo;
		this.meilleur = meilleur;
		this.p_mutation = p_mutation;

	}
	
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
	
	public void arreter() {
		this.running = false;
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
	
		
	public synchronized Individu getBest() {
		return this.best;
	}
	

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
			//System.out.println("best : " + this.best.getLongueur());
		}
		this.population.enleverDoublons();
	
		if(this.iteration%100 == -1) {
			System.out.println(this.population.toString());
			
			
		}
		if(this.iteration%15 == 0) {
			this.migrer();
			this.recuperer();
		}
		
		
		return this.getSolution();
	}

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
	
	public void recuperer() throws Exception {
		
		
			Individu[] recu = this.syncFrom.get();
	
		
		for(int i = 0; i < this.syncFrom.getSize(); i++) {
			this.population.inserer(recu[i]);
		}
		
	}
	

}
