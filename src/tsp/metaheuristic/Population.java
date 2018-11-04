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
	/**
	 * Insère chacun des individus dans la population this, de telle manière que this garde la même taille, et que la population résultante soit meilleure. On insère donc
	 * seulement les individus dont la longueur est inférieure à celle de l'individu ayant la plus grande.
	 * @param pop La population à fusionner avec this
	 * @throws Exception
	 */
	public void fusionner(Population pop) throws Exception {
		
		int n = pop.getNbIndividus();
		int start = 0;
		for(int i = 0; i < n; i++) {
				
				start = this.inserer(pop.get(i), start);
		
		}

	}
	/**
	 * Insère ind dans this, de telle manière à garder un ordre croissant de longueur dans le tableau this.population, et seulement si sa longueur est inférieure à la pire de la population.
	 * @param ind l'individu que l'on veut insérer dans this
	 * @throws Exception
	 */
	public void inserer(Individu ind) throws Exception {
		
			this.inserer(ind, 0);
	}
	
	/**
	 * Insère ind dans this, de telle manière à garder un ordre croissant de longueur dans le tableau this.population, et seulement si sa longueur est inférieure à la pire de la population.
	 * @param ind l'individu que l'on veut insérer dans this
	 * @throws Exception
	 */
	public int inserer(Individu ind, int start) throws Exception{
		
		if(ind.getLongueur() < this.get(this.nbIndividus - 1).getLongueur()) { //On insère ind seulement si cela résulte en une amélioration de la population.
					
					int i = start;
					double l = ind.getLongueur();
				
					while(i < this.nbIndividus && l > this.get(i).getLongueur()) {
						i++;
					}
			
					this.population.add(i, ind);
					this.population.remove(this.nbIndividus); //On enlève le dernier individu si on a inséré ind pour garder une taille de population constante.
					return i;
				}
		else {
			return 0;
		}
	}
	/**
	 * Mute chacun des individus de this, avec une certaine probabilité, contenue dans Individu.muter()
	 * @throws Exception
	 */
	public void muter(double p_mutation) throws Exception {
		
		int n = this.population.size();
		Individu mutant;
		
		for(int i = 0; i < n; i++) {
			
			mutant = new Individu(this.get(i).getSize(), this.get(i).getIndividu().clone(), this.instance);
			mutant.setLongueur(this.get(i).getLongueur());
			//mutant.muter();
			mutant.RMSMutation(p_mutation);
			
			
			if(mutant.getLongueur() < this.population.get(i).getLongueur()) {
				this.getPopulation().set(i, mutant);
				}
		}
	}
	
	
	
	
	public void selectionner() {
		
		
		while(this.nbIndividus < this.population.size()){
			this.population.remove(this.population.size()-1);
		}
	}
	
	/**
	 * Applique l'heuristique 2-opt à tous les individus
	 * @throws Exception
	 */
	public void opt2() throws Exception {
		int n = this.nbIndividus;
		
		for(int i = 2; i < n; i++) {
			this.get(i).opt_2(this.instance);
		}
		this.trier();
	}
	/**
	 * 
	 * @return le tableau des individus de this
	 */
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
			int j = i + 1;
			while(j < this.nbIndividus && this.get(i).getLongueur() == this.get(j).getLongueur()) {
			
		
					this.population.set(j, new Individu(this.get(j).getSize(), this.instance));
					j++;
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
