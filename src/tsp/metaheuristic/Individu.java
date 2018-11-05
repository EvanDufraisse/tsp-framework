package tsp.metaheuristic;

import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import tsp.Instance;
import tsp.Solution;

// TODO: Auto-generated Javadoc
/**
 * The Class Individu.
 */
public class Individu {

	/** Le tableau représentant l'ordre de visite des villes. Par exemple : {1, 4, 2, 3, 1}. On visite la ville 1, puis la ville 4, etc... */
	private int[] individu;
	
	/** Le nombre de villes que l'on doit visiter. */
	private int size;
	
	/** La longueur du parcours représenté par l'individu. */
	private double longueur;
	
	/** The instance. */
	private Instance instance;
	
	/**
	 * Instantiates a new individu.
	 *
	 * @param size la taille de l'individu
	 * @param individuVide argument ajouté pour signifier que l'on veut créer un individu vide (pas de perte de temps en créant inutilement un individu aléatoire)
	 * @param instance the instance
	 */
	public Individu(int size, boolean individuVide, Instance instance) {
		this.size = size;
		this.individu = new int[this.size + 1];
		this.instance = instance;
	}
	
	
	/**
	 * Instantiates a new individu.
	 *
	 * @param size la taille de l'individu
	 * @param instance the instance
	 * @throws Exception the exception
	 */
	public Individu(int size, Instance instance) throws Exception {
		// TODO Auto-generated constructor stub
		this.size = size;
		this.individu = individuAleatoire(this.size).getIndividu();
		this.instance = instance;
		this.setLongueur();
		
	}
	
	/**
	 * Créé un individu ayant pour tableau des villes celui passé en paramètre.
	 *
	 * @param n la taille de l'individu
	 * @param individu the individu
	 * @param instance the instance
	 */
	public Individu(int n, int[] individu, Instance instance) {
		this.individu = individu;
		this.size = n;
		this.instance = instance;
	}

	/**
	 * Créé un individu par recopie profonde.
	 *
	 * @param ind L'individuu que l'on veut cloner.
	 */
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
	
	
	/**
	 * Mute l'individu, en échangeant un nombre de villes aléatoire.
	 *
	 * @throws Exception the exception
	 */
	public void muter() throws Exception {
		
		double p_mutation = 0.15; // la probabilité de mutation d'un individu
		//int proportion_mutation = (int)(this.size/20); //la proportion d'alleles que l'on mute
		int proportion_mutation = randomWithRange(1, 5);
		
		if(Math.random() < p_mutation) {
		
			for(int i = 0; i < proportion_mutation; i++) {
				swap(this.individu, randomWithRange(1, this.size - 1), randomWithRange(1, this.size - 1)); //On echange des ville aleatoirement choisies (sauf la premiere)
			}
			this.opt_2(this.instance);
		
		}
		
		
	}
	
	/**
	 * Mute l'individu par mutation RMS.
	 *
	 * @param p_mutation la probabilité de mutation.
	 * @throws Exception the exception
	 */
	public void RMSMutation(double p_mutation) throws Exception{
		
		
		if(Math.random() < p_mutation) {
			
			int a = randomWithRange(1, this.size - 1);
			int b = randomWithRange(1, this.size - 1);
			
			if(b < a) {
				int t = a;
				a = b;
				b = t;
			}
			
			while(a < b) {
				this.swapInd(a, b);
				a++;
				b--;
			}
				
		}
		this.opt_2(this.instance);
	}
	
	/**
	 * 
	 *
	 * @return Le tableau représentant l'ordre de parcours des villes.
	 */
	public int[] getIndividu() {
		return this.individu;
				
	}
	
	
	
	
	/**
	 * 
	 *
	 * @param min L'entier minimum que l'on veut retourner.
	 * @param max L'entier maximum que l'on veut retourner.
	 * @return Un entier aléatoirement choisi entre min et max inclus.
	 */
	int randomWithRange(int min, int max)
	{
	   double range = Math.abs(max - min);     
	   return (int)(Math.random() * range) + (min <= max ? min : max);
	}
	
	/**
	 * 
	 *
	 * @return La taille de l'individu.
	 */
	public int getSize() {
		return this.size;
	}
	
	/**
	 * 
	 *
	 * @param indice La place de la ville dans le parcours que l'on veut affecter.
	 * @param elt Le numéro de la ville que l'on veut placer dans le parcours.
	 */
	public void set(int indice, int elt) {
		this.individu[indice] = elt;
	}
	
	/**
	 * 
	 *
	 * @param i La place dans le parcours dont on veut retourner le numéro de la ville.
	 * @return Le numéro de la ville qui a la place i dans le parcours.
	 */
	public int get(int i) {
		return this.individu[i];
	}
	
	/**
	 * Gets the instance of Individu.
	 *
	 * @return the instance of Individu
	 */
	public Instance getInstance() {
		return this.instance;
	}
	
	/**
	 * 
	 *
	 * @param Le numéro de la ville.
	 * @return true si la ville elt est dans l'individu, false sinon.
	 */
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
	 *
	 * @param parent2 l'individu avec lequel on croise l'individu sur lequel on applique croiser
	 * @return un individu issu du croisement des deux parents
	 * @throws Exception the exception
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
	
	/**
	 * OX crossover.
	 *
	 * @param parent2 L'individu avec lequel on veut croiser this.
	 * @return l'individu résultant du croisement dit "OX crossover" entre this et parent2.
	 */
	public Individu OXCrossover(Individu parent2) {
		
		int n = this.getSize();
		Individu fils = new Individu(n, true, this.instance);
		//System.out.println("parent 1 :" + this.ecrire());
		//System.out.println("parent 2 :" + parent2.ecrire());
		int x = 1;
		int y = 0;
		
		while(y <= x) {
			x = randomWithRange(1, n);
			y = randomWithRange(1, n);
		}
		//System.out.println(x);
		//System.out.println(y);
		
		for(int i = x; i <= y; i++) {
			fils.set(i, this.get(i));
		}
		//System.out.println("fils : " + fils.ecrire());
		int nbElementsAjoutesAGauche = 1;
		int i = 1;
		
		while(nbElementsAjoutesAGauche < x ) {
			if(!fils.contains(parent2.get(i))) {
				fils.set(nbElementsAjoutesAGauche, parent2.get(i));
				nbElementsAjoutesAGauche++;
				//System.out.println("fils : " + fils.ecrire());
			}
			i++;
			
		}
		//System.out.println("i = " + i);
		int nbElementsAjoutesADroite = 0;
		int j = y + 1;
		
		while(nbElementsAjoutesADroite < n - y - 1) {
			if(!fils.contains(parent2.get(i))) {
				fils.set(j + nbElementsAjoutesADroite, parent2.get(i));
				nbElementsAjoutesADroite ++;
				//System.out.println("fils : " + fils.ecrire());
			}
			i++;
		}
		//System.out.println("-----------------------------");
		try {
			fils.opt_2(this.instance);
			//fils.setLongueur();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return fils;
	}
	
	/**
	 * Applique l'algorithme du 2-opt à l'individu.
	 *
	 * @param instance the instance
	 * @throws Exception the exception
	 */
	public void opt_2(Instance instance) throws Exception {
		int n = this.getSize();
		
		for(int i = 0; i < n-1; i++) {
			for(int j = 1; j < i-1; j++) {
				if(this.instance.getDistances(this.get(i), this.get(i+1)) + this.instance.getDistances(this.get(j), this.get(j + 1)) > this.instance.getDistances(this.get(i), this.get(j)) + this.instance.getDistances(this.get(j+1), this.get(i+1))) {
					this.swapEdges(i, j);
					
				}
			}
			for(int j = i + 2; j < n; j++) {
				
				if(this.instance.getDistances(this.get(i), this.get(i+1)) + this.instance.getDistances(this.get(j), this.get(j + 1)) > this.instance.getDistances(this.get(i), this.get(j)) + this.instance.getDistances(this.get(j+1), this.get(i+1))) {
					
					this.swapEdges(i, j);
					
				}
			}
		}
		
		this.setLongueur();
		
	}
	
	
	/**
	 * Echange les elements de a situés aux places i et j.
	 *
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
	 * Echange les segments [i; i + 1] et [j; j + 1].
	 *
	 * @param i une ville du parcours
	 * @param j une ville du parcours
	 */
	public void swapEdges(int i, int j) {
		
		int nbSwaps = (int)((j - i + 1)/2); //Nombre de swaps effectués
		
		for(int ind = 0; ind < nbSwaps; ind++) {
			swap(this.getIndividu(), i + 1 + ind, j - ind);
		}
		
	}
	
	/**
	 * Echange les villes situées aux places i et j.
	 *
	 * @param i la place de la ville que l'on veut échanger avec celle d'indice j.
	 * @param j la place de la ville que l'on veut échanger avec celle d'indice i.
	 */
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
	 * @param n the n
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
	
	/**
	 * Recalcule la longueur de l'individu.
	 *
	 * @throws Exception the exception
	 */
	public void setLongueur() throws Exception {
		
		double longueurTemp = 0;
		
		for(int i = 0; i < this.size; i++) {
			longueurTemp += this.instance.getDistances(this.getIndividu()[i], this.getIndividu()[i + 1]);
		}
		this.longueur = longueurTemp;
	}
	
	/**
	 * Change la longueur de l'individu en lon.
	 *
	 * @param lon la longueur que l'on veut affecter à l'individu.
	 */
	public void setLongueur(double lon) {
		this.longueur = lon;
	}
	
	/**
	 * 
	 *
	 * @return La longueur de l'individu.
	 */
	public double getLongueur() {
		return this.longueur;
	}
	
	/**
	 *
	 *
	 * @return Une chaine de caractères correspondant à la concaténation des numéros des villes dans l'ordre de parcours.
	 */
	public String ecrire() {
		String s = "";
		for(int i = 0; i <= this.size; i++) {
			s += " " + this.individu[i] + " ";
		}
		
		return s;
	}
	
	/**
	 * Gets the max.
	 *
	 * @return the 
	 */
	/*public int getMax( {
		int max = 0;
		for (int i = 0; i < this.getIndividu().length; i++) {
			if(this.getIndividu()[i] > max) {
				max = this.getIndividu()[i];
			}
		}
		
		return max;
	}*/
	
	/**
	 * 
	 *
	 * @return Un objet Solution correspondant à l'individu.
	 * @throws Exception the exception
	 */
	public Solution toSolution() throws Exception {
		
	Solution solution = new Solution(this.instance);
			
			for(int i = 0; i < this.instance.getNbCities(); i++) {
				solution.setCityPosition(i, this.individu[i]);
			}
			return solution;
		
	}

}