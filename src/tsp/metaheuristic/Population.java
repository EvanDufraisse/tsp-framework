package tsp.metaheuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import tsp.Instance;


public class Population {

	
	private ArrayList<Individu> population;
	private int nbIndividus;
	
	public Population(int nbIndividus, int nbVilles) {
		this.nbIndividus = nbIndividus;
		this.population = new ArrayList<Individu>();
		for(int i = 0; i < nbIndividus; i++) {
			
			this.population.add(new Individu(nbVilles));
			
		}
		
	}
	public Population(ArrayList<Individu> population, int nbIndividus) {
		this.population = population;
		this.nbIndividus = nbIndividus;
		
	}
	
	public Individu get(int i) {
		return this.population.get(i);
	}
	
	public void trier(Instance instance) {
		
	
	Collections.sort(population,new Comparator<Individu>(){
		   @Override
		   public int compare(final Individu lhs,Individu rhs) {
			   double LongueurLHS = 0;
			   double LongueurRHS = 0;
			   try {
				LongueurLHS = lhs.getLongueur(instance);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			   try {
				LongueurRHS = rhs.getLongueur(instance);
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
	public void fusionner(Population pop) {
		for(int i = 0; i < pop.nbIndividus; i++) {
			this.population.add(i, pop.population.get(i));
		}
	}
	
	/**
	 * 
	 * @param nbIndividusSelectionnes le nombre d'individus que l'on veut croiser entre eux
	 * @param instance
	 * @return une population constituée des nbIndividusSelectionnes meilleurs individus
	 */
	public Population selectionnerCroiser(int nbIndividusSelectionnes, Instance instance) {
		this.trier(instance);
		ArrayList<Individu> selection = new ArrayList<Individu>();
		for(int i = 0; i < nbIndividusSelectionnes; i++) {
			selection.add(this.population.get(i).croiser(this.population.get(i+1)));
			selection.add(this.population.get(i+1).croiser(this.population.get(i)));
		}
		
		return new Population(selection, nbIndividusSelectionnes);
			
		}
}
