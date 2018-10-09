package tsp.heuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;

import tsp.Instance;

public class Individu {

	private int[] individu;
	private int size;
	
	public Individu(int n, boolean individuVide) {
		this.size = n;
		this.individu = new int[n];
	}
	
	
	public Individu(int n) {
		// TODO Auto-generated constructor stub
		this.size = n;
		this.individu = individuAleatoire(this.size);
	}
	
	
	public int[] getIndividu() {
		return this.individu;
				
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
	 */
	public Individu croiser (Individu parent2){
		
		int n = this.getSize();
		
		Individu fils = new Individu(n);
		
		for(int i = 0; i < (int)(n/2); i++) {
			fils.set(i, parent2.get(i));
		}
		int nbElementsAjoutes = 0;
		int i = (int)(n/2);
		while(nbElementsAjoutes < n) {
			if(!fils.contains(parent2.get(i))) {
				fils.set(i + nbElementsAjoutes, parent2.get(i));
			}
			i++;
		}
		
		
		return fils;
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
	 * Créé un individu aléatoire (c'est-à-dire un tableau d'entiers
	 * choisis aléatoirement entre 1 et n) et apparaissant une et une
	 * seule fois.
	 * 
	 * @return individu solution aléatoirement choisi
	 */
	public int[] individuAleatoire(int n) {
		
		int[] individu = new int[n];
		
		for(int i = 0; i < n; i++) {
			individu[i] = i + 1;
		}
		
		int j = 0;
		
		for(int i = n - 1; i >=0 ; i-- ) {
			j = ThreadLocalRandom.current().nextInt(0, i + 1);
			swap(individu, i, j);
		}
		
		return individu;
	}
	
	public double getLongueur(Instance instance) throws Exception {
		
		double longueur = 0;
		
		for(int i = 0; i < this.size - 1; i++) {
			longueur += instance.getDistances(i, i+1);
		}
		return longueur;
	}

}
