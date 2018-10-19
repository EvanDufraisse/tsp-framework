package tsp.metaheuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tsp.Instance;


public class Population {

	
	private ArrayList<Individu> population;
	private int nbIndividus;
	private Instance instance;
	
	public Population(int nbIndividus, int nbVilles, Instance instance) {
		
		this.nbIndividus = nbIndividus;
		this.population = new ArrayList<Individu>();
		this.instance = instance;
		
		for(int i = 0; i < nbIndividus; i++) {
			
			this.population.add(new Individu(nbVilles, this.instance));
			
		}
		
	}
	public Population(ArrayList<Individu> population, int nbIndividus, Instance instance) {
		this.population = population;
		this.nbIndividus = nbIndividus;
		this.instance = instance;
	}
	
	public Individu get(int i) {
		return this.population.get(i);
	}
	public int getNbIndividus() {
		return this.nbIndividus;
	}
	
	public void trier() {
		
	
	Collections.sort(population,new Comparator<Individu>(){
		   @Override
		   public int compare(final Individu lhs,Individu rhs) {
			   double LongueurLHS = 0;
			   double LongueurRHS = 0;
			   try {
				LongueurLHS = lhs.getLongueur();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   try {
				LongueurRHS = rhs.getLongueur();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(LongueurRHS < LongueurLHS) {
				return 1;
			}
			else if(LongueurRHS == LongueurLHS) {
				return 0;
			}
			else {
				return -1;
			}
		     //TODO return 1 if rhs should be before lhs 
			   
		     //     return -1 if lhs should be before rhs
		     //     return 0 otherwise
			   
		     }
		 });
	
	

}
	public void fusionner(Population pop, Instance instance) throws Exception {
		double longueur = 0;
		double longueurMax = this.population.get(this.getNbIndividus() - 1).getLongueur();
		for(int i = 0; i < pop.getNbIndividus(); i++) {
			longueur = pop.population.get(i).getLongueur();
			if(longueur  <  longueurMax) {
				//pop.opt2(instance);
				
				
				this.inserer(pop.population.get(i), instance);
			}
			//this.population.add(pop.population.get(i));
		}
	}
	public void inserer(Individu ind, Instance instance) throws Exception {
		int i = 0;
		double l = ind.getLongueur();
		
		while(i < this.population.size() && l > this.population.get(i).getLongueur()) {
			i++;
		}
		this.population.set(i, ind);
	}
	public void muter(Instance instance) throws Exception {
		int n = this.population.size();
		Individu mutant;
		for(int i = 0; i < n; i++) {
			
			mutant = new Individu(this.get(i).getSize(), this.get(i).getIndividu().clone(), instance);
			mutant.setLongueur();
			if(mutant.muter()) {
				mutant.opt_2(instance);
			}
			
			//mutant.opt_2(instance);
			this.inserer(mutant, instance);*/
			
			this.population.get(i).muter();
			//this.inserer(this.population.get(i), instance);
		
			//this.inserer(mutant, instance);
			//this.population.get(i).opt_2(instance);
		}
	}
	
	public void selectionner() {
		
		int n = this.population.size();
		while(n < this.population.size()){
			this.population.remove(n);
		}
	}
	
	public void opt2(Instance instance) throws Exception {
		for(int i = 1; i < this.nbIndividus; i++) {
			this.population.get(i).opt_2(instance);
		}
	}
	
	public ArrayList<Individu> get() {
		return this.population;
	}
	/**
	 * 
	 * @param nbIndividusSelectionnes le nombre d'individus que l'on veut croiser entre eux
	 * @param instance
	 * @return une population constituée des nbIndividusSelectionnes meilleurs individus
	 * @throws Exception 
	 */
	public Population selectionnerCroiser(int nbIndividusSelectionnes, Instance instance) throws Exception {
		this.trier();
		ArrayList<Individu> selection = new ArrayList<Individu>();
		for(int i = 0; i < nbIndividusSelectionnes; i++) {
			
			selection.add(this.population.get(2*i).croiser(this.population.get(2*i+1)));
			selection.add(this.population.get(2*i+1).croiser(this.population.get(2*i)));
		}
		
		return new Population(selection, nbIndividusSelectionnes, this.instance);
			
		}
}
