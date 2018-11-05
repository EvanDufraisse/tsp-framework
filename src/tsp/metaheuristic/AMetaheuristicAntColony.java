package tsp.metaheuristic;

import tsp.Instance;
import tsp.Solution;

public class AMetaheuristicAntColony extends AMetaheuristic{
	
	private int nbFourmis;
	private double vitesseEvaporation;
	private float alpha;
	private float beta;
	private int Q;
	private int NCmax;
	private double c;
	
	/**
	 * Constructeur qui initialise les paramètres du problème pour la résolution par colonie de fourmis
	 * 
	 * @param instance
	 * @param name
	 * @throws Exception
	 */
	
	public AMetaheuristicAntColony(Instance instance, String name) throws Exception {
		super(instance,name);
		this.nbFourmis=20;
		this.vitesseEvaporation=0.5;
		this.alpha=1;
		this.beta=5;
		this.Q=1000;
		this.NCmax=1500;
		this.c=0.001;
	}
	
	
	/**
	 * Méthode qui retourne un booléen present attestant de l'appartenance de l'entier i à la liste liste à un indice inférieur à k
	 * 
	 * @param liste
	 * @param i
	 * @param k
	 * @return present
	 */

	public boolean estDansListe(int[] liste, int i, int k) {
		boolean present=i==liste[0];
		int j=1;
		while (!present && j<k) {
			present=i==liste[j];
			j++;
		}
		return present;
	}
	
	
	/**
	 * Méthode qui retourne le parcours liste changé après une itération, on ajoute une ville au parcours à l'indice j dans liste
	 * La probabilté intervient ici pour déterminer la ville suivante, probabilité dépendant de la visibilité (1/distance) et de la phéromone
	 * C'est une itération pour une fourmi
	 * 
	 * @param liste
	 * @param j
	 * @param pistePheromone
	 * @return liste qui correspond au parcours précédent mais avec une ville en plus
	 */
	
	public int[] listeApresUneIteration(int[] liste, int j, double[][] pistePheromone) {
		// On initialise la liste de probabilités en mettant une probabilité non nulle si la ville n'est pas encore visitée dans liste
		double[] listeProba= new double[this.m_instance.getNbCities()];
		int nbVillesNonVisitees=0;
		for (int i=0; i<listeProba.length; i++) {
			if (estDansListe(liste,i,j) || i==j-1) {
				listeProba[i]=0;
			} else {
				listeProba[i]=Math.pow(pistePheromone[j-1][i], this.alpha)*Math.pow((1.0/this.m_instance.getDistances()[j-1][i]), this.beta);					
				nbVillesNonVisitees++;
			}
		}
		
		/* On crée une liste des probabilités non nulles avec l'entier de la ville qui correspond,
		 avec pour longueur le nb de villes non visitées encore */
		double[][] listeProbaNonNulles=new double[nbVillesNonVisitees][2];
		int k=0;
		for (int i=0; i<listeProba.length; i++) {
			if (listeProba[i]!=0) {
				listeProbaNonNulles[k][0]=i;
				listeProbaNonNulles[k][1]=listeProba[i];
				k++;
			}
		}
		
		/* On pondère les probabilités pour donner plus d'importance aux probabilités plus grandes
		 listeProbaPond[i]=listeProbaPond[i-1]+listeProbaNonNulles[i] */
		double[] listeProbaPond=new double[listeProbaNonNulles.length];
		double sumProb=0;
		for (int i=0; i<listeProbaPond.length; i++) {
			sumProb+=listeProbaNonNulles[i][1];
			listeProbaPond[i]=sumProb;
		}
		
		
		//On utilise ici un random entre 0 et la somme des probas et on voit à quelle proba on tombe
		double random=Math.random()*sumProb;
		
		int i=0;
		double somme=listeProbaPond[i];
		while (random>somme) {
			i++;
			somme=listeProbaPond[i];
		}
		
		liste[j]=((int)listeProbaNonNulles[i][0]);
		return liste;
	}
	
	
	/**
	 * Méthode qui retourne le tableau des parcours terminés de toutes les fourmis
	 * 
	 * @param liste
	 * @param pistePheromone
	 * @return listeApresNIterations les parcours complets
	 */
	
	public int[][] listeApresNIterations(int[][] liste, double[][] pistePheromone) {
		
		//On fait le parcours de chaque fourmi i et à chaque indice j on ajoute une ville au parcours de la fourmi i
		for (int i=0; i<liste.length; i++) {
			for (int j=1; j<liste[i].length-1; j++) {
				liste[i]=listeApresUneIteration(liste[i],j,pistePheromone);
			}
			int j=0;
			while (estDansListe(liste[i],j,liste[i].length-1)) {
				j++;
			}
			liste[i][liste[i].length-1]=j;
		}
		return liste;
	}
	
	
	/**
	 * Méthode qui retourne la longueur du parcours ayant en paramètre le parcours complet, 
	 * grâce au tableau des this.m_instance.getDistances()
	 * 
	 * @param liste
	 * @return la longueur du parcours
	 */
	
	public double calculLongueurParcours(int[] liste) {
		double longueur=0;
		for (int i=1; i<liste.length; i++) {
			longueur+=this.m_instance.getDistances()[liste[i-1]][liste[i]];
		}
		longueur+=this.m_instance.getDistances()[liste[liste.length-1]][liste[0]];
		return longueur;
	}
	
	
	/**
	 * Méthode qui retourne un booléen estVisite disant si oui ou non l'arc (i,j) est visité dans le parcours parcours
	 * 
	 * @param parcours
	 * @param i
	 * @param j
	 * @return estVisite
	 */
	
	public boolean arcEstVisite(int[] parcours, int i, int j) {
		boolean estVisite=false;
		int n=parcours.length;
		int k=0;
		while (!estVisite && k<n-1) {
			estVisite=(parcours[k]==i && parcours[k+1]==j);
			k++;
		}
		if (!estVisite) {
			estVisite=(parcours[n-1]==i && parcours[0]==j);
		}
		return estVisite;
	}
	
	
	/**
	 * Méthode qui retourne l'indice de la valeur val dans le parcours parcours 
	 * 
	 * @param val
	 * @param parcours
	 * @return i l'indice 
	 */
	
	public int indiceValDansParcours(int val, int[] parcours) {
		int i=0;
		while (parcours[i]!=val) {
			i++;
		}
		return i;
	}
	
	
	/**
	 * Méthode qui teste si deux fourmis font le même tour, sans forcément partir du même point de départ
	 * 
	 * @param parc1 parcours terminé
	 * @param parc2 parcours terminé
	 * @return memeTour booléen qui atteste du même tour entre les deux parcours
	 */
	
	
	public boolean deuxFourmisFontMemeTour(int[] parc1, int[] parc2) {
		boolean memeTour=true;
		int i=0;
		int j=indiceValDansParcours(parc1[0],parc2);
		while (memeTour && j<parc2.length) {
			memeTour=parc1[i]==parc2[j];
			i++;
			j++;
		}
		if (memeTour) {
			j=0;
			while(memeTour && j<indiceValDansParcours(parc1[0],parc2)) {
				memeTour=parc1[i]==parc2[j];
				i++;
				j++;
			}
		}
		return memeTour;
	}
	
	
	/**
	 * Méthode qui retourne en String un parcours d'entiers
	 * 
	 * @param parc
	 * @return
	 */
	
	
	public String toString(int[] parc ) {
		String res="{";
		for (int i=0; i<parc.length-1; i++) {
			res+=parc[i]+",";
		}
		return res+parc[parc.length-1]+"}";
	}
	
	
	/**
	 * Méthode qui retourne en String un parcours de double
	 * 
	 * @param parc
	 * @return
	 */
	
	public String toString1(double[] parc ) {
		String res="{";
		for (int i=0; i<parc.length-1; i++) {
			res+=parc[i]+",";
		}
		return res+parc[parc.length-1]+"}";
	}
	
	
	/**
	 * Méthode qui teste si toutes les fourmis font le même tour, condition d'arrêt du programme
	 * 
	 * @param listeVillesParcourues
	 * @return
	 */
	
	public boolean toutesFourmisFontMemeTour(int[][] listeVillesParcourues) {
		boolean memeTour=true;
		int n=1;
		while(memeTour && n<listeVillesParcourues.length) {
			memeTour=deuxFourmisFontMemeTour(listeVillesParcourues[0],listeVillesParcourues[n]);
			n++;
		}
		return memeTour;
	}
	
	
	/**
	 * Méthode principale qui retourne la meilleure solution trouvée en terme de longueur de parcours
	 * 
	 * @return
	 * @throws Exception
	 */
	
	public Solution getSolution() throws Exception {
		
		//On initialise le temps
		long t0 = System.currentTimeMillis();
		long t = 0;
		
		Solution solution = new Solution(this.m_instance);
		/*Initialisation
		crée liste mémoire des villes parcourues par chaque fourmi (fourmi i correspond à la ligne i du tableau) */
		int[][] listeVillesParcourues= new int[this.nbFourmis][this.m_instance.getNbCities()];
		for (int i=0; i<this.nbFourmis; i++) {
			
			// On met chaque fourmi à sa ville de départ
			listeVillesParcourues[i][0]=i;	
		}
		
		//On initialise Lkmin et parcoursMin
		double Lkmin=1000000;
		int[] parcoursMin=new int[this.m_instance.getNbCities()];
		
		// On initialise les pistes de phéromones à this.c
		double[][] pistePheromone= new double[this.m_instance.getNbCities()][this.m_instance.getNbCities()];
		for (int j=0; j<this.m_instance.getNbCities(); j++) {
			for (int k=0; k<this.m_instance.getNbCities();k++) {
				pistePheromone[j][k]=this.c;
			}	
		}
		

		//On effectue au max NCmax cycles, l'arrêt se fait sinon si fourmis prennent toutes même parcours ou si dépassement du temps
		int n=0;
		boolean memeTourPourToutLeMonde=false;
		long t1=0;
		while (n<this.NCmax && !memeTourPourToutLeMonde && t<59000-t1) {
			
			//On trouve désormais les listes mémoires pleines (un cycle a été effectué)
			listeVillesParcourues=this.listeApresNIterations(listeVillesParcourues, pistePheromone);
			
			//Calculs après 1 cycle
			
			//Calcul longueur des parcours
			double[] longueur= new double[this.nbFourmis];
			for (int i=0; i<this.nbFourmis; i++) {
				longueur[i]=this.calculLongueurParcours(listeVillesParcourues[i]);
			}
			
			//Mise à jour des phéromones
			for (int i=0; i<pistePheromone.length; i++) {
				for (int j=0; j<pistePheromone[i].length; j++) {
					pistePheromone[i][j]=pistePheromone[i][j]*this.vitesseEvaporation;
					for (int k=0; k<longueur.length; k++) {
						if (arcEstVisite(listeVillesParcourues[k],i,j)) {
							pistePheromone[i][j]+=this.Q/longueur[k];
						}
					}
				}
			}
			
			/*Calcul du Lk minimum (longueur minimum des parcours des nbFourmis fourmis)
			 * Si IndexMin=-1 à la fin, c'est qu'on a pas trouvé un parcours plus optimal
			 * Dans le parcoursMin, on ne met pas encore la dernière ville égale à la première car 0 n'est pas forcément première ville
			 */
			int indexMin=-1;
			for (int i=0; i<longueur.length; i++) {
				if (longueur[i]<=Lkmin) {
					Lkmin=longueur[i];
					indexMin=i;
				}
			}
			if(indexMin!=-1) {
				for (int i=0; i<this.m_instance.getNbCities(); i++) {
					parcoursMin[i]=listeVillesParcourues[indexMin][i];
				}
			}
			
			//On affiche le meilleur parcours actuel avec sa longueur 
			System.out.println("indexMin=  "+indexMin+" Lkmin= "+Lkmin+" parcoursMin= "+toString(parcoursMin));
			
			//Booléen pour savoir si tout le monde fait le même tour change
			memeTourPourToutLeMonde=toutesFourmisFontMemeTour(listeVillesParcourues);
			
			//Liste mémoires effacées et nouveau tour avec même ville de départ pour chaque fourmi
			for (int i=0; i<listeVillesParcourues.length; i++) {
				listeVillesParcourues=new int[this.nbFourmis][this.m_instance.getNbCities()];
				listeVillesParcourues[i][0]=i;
			}
			
			//On augmente n
			n++;
			t = System.currentTimeMillis() - t0;
			t1=t/n;
			System.out.println("n= "+n+" t= "+t+" memeTourPourLeMonde= "+memeTourPourToutLeMonde);
			

		}
	
		//On prend le meilleur parcours et on le change de sorte que la première ville soit 0 mais on garde le même parcours
		//On ajoute la ville 0 en dernier pour respecter les conditions (départ et arrivée en 0)
		int[] parcoursMin1=new int[this.m_instance.getNbCities()+1];
		int l=0;
		while (0!=parcoursMin[l]) {
			l++;
		}
		for (int k=0; k<parcoursMin.length-l;k++) {
			parcoursMin1[k]=parcoursMin[k+l];
		}
		for (int k=parcoursMin.length-l; k<parcoursMin.length; k++) {
			parcoursMin1[k]=parcoursMin[k+l-parcoursMin.length];
		}
		parcoursMin1[this.m_instance.getNbCities()]=0;
		
		
		// On met le parcours minimal dans notre solution
		for(int i = 0; i <= this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(parcoursMin1[i], i);
		}
		
		solution.setObjectiveValue((long)Lkmin);
		
		
		return solution;
	}
	
	
	@Override
	public Solution solve(Solution sol) throws Exception {
		// TODO Auto-generated method stub
		return this.getSolution();
	}
	
	
	
}
