package tsp.metaheuristic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.sun.tools.javac.util.List;

import tsp.Instance;


public class Population {

	
	private ArrayList<Individu> population;
	private int nbIndividus;
	private Instance instance;
	
	public Population(int nbIndividus, int nbVilles, Instance instance) throws Exception {
		
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
	public ArrayList<Individu> getPopulation(){
		return this.population;
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
		
		int n = pop.getNbIndividus();
		for(int i = 0; i < n; i++) {
				
				this.inserer(pop.get(i), instance);
			
				//this.population.add(pop.population.get(i));
		}
		//this.trier();
	}
	public void inserer(Individu ind, Instance instance) throws Exception {
		int i = 0;
		double l = ind.getLongueur();
		
		while(i < this.nbIndividus && l > this.get(i).getLongueur()) {
			i++;
		}
		//this.population.set(i, ind);
		this.population.add(i, ind);
		this.population.remove(this.nbIndividus);
	}
	public void muter(Instance instance) throws Exception {
		int n = this.population.size();
		Individu mutant;
		for(int i = 0; i < n; i++) {
			///*
			mutant = new Individu(this.get(i).getSize(), this.get(i).getIndividu().clone(), instance);
			mutant.setLongueur(this.get(i).getLongueur());
			mutant.muter();
			
			
			if(mutant.getLongueur() < this.population.get(i).getLongueur()) {
				this.getPopulation().set(i, mutant);
				
			}
			//this.inserer(mutant, instance);
			
		}
	}
	
	
	
	
	public void selectionner() {
		
		
		while(this.nbIndividus < this.population.size()){
			this.population.remove(this.population.size()-1);
		}
	}
	
	public void opt2(Instance instance) throws Exception {
		int n = this.nbIndividus;
		//n = (int)(this.nbIndividus/10);
		for(int i = 2; i < n; i++) {
			this.get(i).opt_2(instance);
		}
		this.trier();
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
		//this.trier();
		ArrayList<Individu> selection = new ArrayList<Individu>();
		for(int i = 0; i < nbIndividusSelectionnes; i++) {
			
			selection.add(this.population.get(i).OXCrossover(this.population.get(i+1)));
			//selection.add(this.population.get(2*i+1).croiser(this.population.get(2*i)));
		}
		
		return new Population(selection, nbIndividusSelectionnes, this.instance);
			
		}
	public Population selectionAleatoireCroiser(int nbIndividusSelectionnes) throws Exception {
		
		ArrayList<Individu> selection = new ArrayList<Individu>();
		int indiceA = 0;
		int indiceB = 0;
		for(int i = 0; i < nbIndividusSelectionnes; i++) {
			indiceA = randomWithRange(0, this.getNbIndividus() - 1);
			indiceB = randomWithRange(0, this.getNbIndividus() - 1);
			selection.add(this.population.get(indiceA).OXCrossover(this.population.get(indiceB)));
			//selection.add(this.population.get(indiceB).croiser(this.population.get(indiceA)));
		}
		return new Population(selection, nbIndividusSelectionnes, this.instance);
		
		
		
	}
	public void enleverDoublons() throws Exception {
	
		for(int i = 0; i < this.nbIndividus; i++) {
			for(int j = i+1; j < this.nbIndividus; j++) {
				if(this.get(i).getLongueur() == this.get(j).getLongueur()) {
					this.population.set(j, new Individu(this.get(j).getSize(), this.instance));
				}
			}
			
		}
		this.trier();
	}
		int randomWithRange(int min, int max)
	{
	   double range = Math.abs(max - min);     
	   return (int)(Math.random() * range) + (min <= max ? min : max);
	}
	public String toString() {
		int n = this.population.size();
		String s = "[";
		for(int i = 0; i < n; i ++) {
			s += this.get(i).getLongueur() + " ,";
		}
		return s + "fin";
	}
}
