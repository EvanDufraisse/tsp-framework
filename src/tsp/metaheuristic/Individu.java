package tsp.metaheuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import tsp.Instance;

public class Individu {

	private int[] individu;
	private int size;
	private double longueur;
	private Instance instance;
	
	public Individu(int n, boolean individuVide, Instance instance) {
		this.size = n;
		this.individu = new int[n+1];
		this.instance = instance;
	}
	
	
	public Individu(int n, Instance instance) throws Exception {
		// TODO Auto-generated constructor stub
		this.size = n;
		this.individu = individuAleatoire(this.size).getIndividu();
		this.instance = instance;
		this.setLongueur();
		
	}
	public Individu(int n, int[] individu, Instance instance) {
		this.individu = individu;
		this.size = n;
		this.instance = instance;
	}

	public Individu(Individu ind) {
		this.individu = ind.getIndividu().clone();
		int n = ind.getSize();
		this.individu = new int[ind.getSize() + 1];
		int[] tableau = ind.getIndividu();
		for(int i = 0; i < n; i ++) {
			this.individu[i] = tableau[i];
		}
		this.size = ind.getSize();
		this.longueur = ind.getLongueur();
		this.instance = ind.getInstance();
	}
	
	
	public void muter() throws Exception {
		
		double p_mutation = 0.15; // la probabilité de mutation d'un individu
		//int proportion_mutation = (int)(this.size/20); //la proportion d'alleles que l'on mute
		int proportion_mutation = randomWithRange(1, 3);
		
		if(Math.random() < p_mutation) {
		
			for(int i = 0; i < proportion_mutation; i++) {
				swap(this.individu, randomWithRange(1, this.size - 1), randomWithRange(1, this.size - 1)); //On echange des ville aleatoirement choisies (sauf la premiere)
			}
			this.opt_2(instance);
			this.setLongueur();
		}
		
		
	}
	
	public int[] getIndividu() {
		return this.individu;
				
	}
	
	
	
	
	int randomWithRange(int min, int max)
	{
	   double range = Math.abs(max - min);     
	   return (int)(Math.random() * range) + (min <= max ? min : max);
	}
	
	public int getSize() {
		return this.size;
	}
	public void set(int indice, int elt) {
		this.individu[indice] = elt;
	}
	public int get(int i) {
		return this.individu[i];
	}
	public Instance getInstance() {
		return this.instance;
	}
	public boolean contains(int elt) {
	
		for(int i = 0; i < this.getSize(); i++) {
			if(this.get(i) == elt) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Croise l'individu avec un autre individu appelé parent2.
	 * Donne d'abord la moitié des allèles de l'individu au fils.
	 * Ajoute ensuite les allèles du parent2 si ils ne sont pas déjà
	 * présents dans le fils, auquel cas on essaye d'ajouter l'allèle
	 * suivant.
	 * @param parent2 l'individu avec lequel on croise l'individu sur lequel on applique croiser
	 * @return un individu issu du croisement des deux parents
	 * @throws Exception 
	 */
	public Individu croiser (Individu parent2) throws Exception {
		
		int n = this.getSize();

		Individu fils = new Individu(n, true, this.instance);
		
		for(int i = 0; i < (int)(n/2); i++) {
			fils.set(i, this.get(i));
		}
		
		int nbElementsAjoutes = (int)(n/2);
		int i = 0;
		
		while(nbElementsAjoutes < n) {
			if(!fils.contains(parent2.get(i))) {
				fils.set(nbElementsAjoutes, parent2.get(i));
				nbElementsAjoutes ++;
				
			}
			i++;
	
		}
		fils.setLongueur();
		
		//System.out.println(fils.ecrire());
		return fils;
	}
	public void opt_2(Instance instance) throws Exception {
		int n = this.getSize();
		
		for(int i = 1; i < n-1; i++) {
			for(int j = 1; j < i-1; j++) {
				if(this.instance.getDistances(this.get(i), this.get(i+1)) + this.instance.getDistances(this.get(j), this.get(j + 1)) > this.instance.getDistances(this.get(i), this.get(j)) + this.instance.getDistances(this.get(j+1), this.get(i+1))) {
					this.swapEdges(i, j);
					
				}
			}
			for(int j = i + 2; j < n-1; j++) {
				
				if(this.instance.getDistances(this.get(i), this.get(i+1)) + this.instance.getDistances(this.get(j), this.get(j + 1)) > this.instance.getDistances(this.get(i), this.get(j)) + this.instance.getDistances(this.get(j+1), this.get(i+1))) {
					
					this.swapEdges(i, j);
					
				}
			}
		}
		
		this.setLongueur();
		
	}
	
	
	/**
	 * Echange les elements de a situés aux places i et j
	 * @param a un tableau d'entiers représentant un individu
	 * @param i un indice quelconque du tableau
	 * @param j un indice quelconque du tableau
	 */
	public static final void swap (int[] a, int i, int j) {
		  int t = a[i];
		  a[i] = a[j];
		  a[j] = t;
		}
	/**
	 * Echange les segments [i; i + 1] et [j; j + 1]
	 * @param i une ville du parcours
	 * @param j une ville du parcours
	 */
	public void swapEdges(int i, int j) {
		
		int nbSwaps = (int)((j - i + 1)/2); //Nombre de swaps effectués
		
		for(int ind = 0; ind < nbSwaps; ind++) {
			swap(this.getIndividu(), i + 1 + ind, j - ind);
		}
		
	}
	public void swapInd(int i, int j) {
		int t = this.individu[i];
		this.individu[i] = this.individu[j];
		this.individu[j] = t;
	}
	
	/**
	 * Créé un individu aléatoire (c'est-à-dire un tableau d'entiers
	 * choisis aléatoirement entre 1 et n) et apparaissant une et une
	 * seule fois.
	 * 
	 * @return individu solution aléatoirement choisi
	 */
	public Individu individuAleatoire(int n) {
		
		int[] individuAlea = new int[n + 1];
		
		for(int i = 0; i < n; i++) {
			individuAlea[i] = i;
			
		}
		individuAlea[n] = 0;
		//System.out.println(new Individu(n, individuAlea).ecrire());
		int j = 0;
		
		for(int i = n - 1; i >=1 ; i-- ) {
			Random rand = new Random();
			j = rand.nextInt(i)+1;
			swap(individuAlea, i, j);
		}
		//System.out.println(new Individu(n, individuAlea).ecrire());
		
		return new Individu(n, individuAlea, this.instance);
	}
	
	public void setLongueur() throws Exception {
		
		double longueurTemp = 0;
		
		for(int i = 0; i < this.size; i++) {
			longueurTemp += this.instance.getDistances(this.getIndividu()[i], this.getIndividu()[i + 1]);
		}
		this.longueur = longueurTemp;
	}
	public void setLongueur(double lon) {
		this.longueur = lon;
	}
	
	public double getLongueur() {
		return this.longueur;
	}
	
	public String ecrire() {
		String s = "";
		for(int i = 0; i < this.size; i++) {
			s += " " + this.individu[i] + " ";
		}
		
		return s;
	}
	public int getMax() {
		int max = 0;
		for (int i = 0; i < this.getIndividu().length; i++) {
			if(this.getIndividu()[i] > max) {
				max = this.getIndividu()[i];
			}
		}
		
		return max;
	}

}