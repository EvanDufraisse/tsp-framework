package tsp.metaheuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import tsp.Instance;

public class Individu {

	private int[] individu;
	private int size;
	
	public Individu(int n, boolean individuVide) {
		this.size = n;
		this.individu = new int[n+1];
		
	}
	
	
	public Individu(int n) {
		// TODO Auto-generated constructor stub
		this.size = n;
		this.individu = individuAleatoire(this.size).getIndividu();
		
	}
	public Individu(int n, int[] individu) {
		this.individu = individu;
		this.size = n;
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
		//System.out.println("n = " + n);
		//System.out.println("size = " + this.individu.length);
		//System.out.println("Taille parent2 = " + parent2.getSize());
		Individu fils = new Individu(n, true);
		//System.out.println("Taille fils = " + fils.getSize());
		for(int i = 0; i < (int)(n/2); i++) {
			fils.set(i, parent2.get(i));
		}
		
		//System.out.println("n/2 = " + (int)(n/2));
		int nbElementsAjoutes = (int)(n/2);
		//int i = (int)(n/2) - 1;
		int i = 0;
		
		while(nbElementsAjoutes < n) {
			if(!fils.contains(parent2.get(i))) {
				fils.set(nbElementsAjoutes, parent2.get(i));
				nbElementsAjoutes ++;
				
			}
			i++;
	
		}
		
		//System.out.println(fils.ecrire());
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
		
		return new Individu(n, individuAlea);
	}
	
	public double getLongueur(Instance instance) throws Exception {
		
		double longueur = 0;
		
		for(int i = 0; i < this.size-1; i++) {
			longueur += instance.getDistances(this.getIndividu()[i], this.getIndividu()[i + 1]);
		}
		return longueur;
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