
	package tsp.heuristic;

	import java.util.ArrayList;
	import java.util.Arrays;
	import java.util.List;

import tsp.Instance;

	/**
	 * The Class LKH_classic.
	 */
	public class LKH_classic2 extends AHeuristic{
/*##############################################################################################################################*/
/*----------------------------------------------------VARIABLES D'INSTANCES----------------------------------------------------*/
/*############################################################################################################################*/
		
		
	/** The m 1 tree. */
	public int[][] m_1_Tree; /*m_1_Tree est un tableau binaire de taille n*n avec m_1_Tree[i][j] == 1 si la liaison 
	i----j apartient à l'arbre et 0 sinon */

	/** The m topological prec spanning tree. */
	 public List<Integer> m_topological_prec_spanning_tree;/*Cette variable d'instance permet de connaitre l'ordre dans lequel
	les villes ont été ajoutée au Spanning-Tree (Tri topologique), arbre qui précède la création du 1-Tree, ceci est utile pour calculer
	m_alpha_nearness et m_dad ultérieurement */

	/** The m dad. */
	public int[] m_dad;/*m_dad est une liste qui indique par m_dad[City1] la ville parent de City1 dans le Spanning-Tree*/

	/** The m special node. */
	public int m_special_node;/*Il s'agit du noeud "spécial" qui permet de former le 1-Tree à partir du Spanning-Tree*/

	/** The m alpha nearness. */
	public long[][] m_alpha_nearness; /*Il s'agit d'une distance non euclidienne qui permet de savoir quels sont les 
	noeuds les plus probablement reliés ensemble*/

	/** The alpha candidates. */
	 int[][] alpha_candidates;/*Il s'agit d'un tableau obtenu à partir de m_alpha_nearness de taille n*K ou K désigne 
	le nombre de meilleurs candidats que l'on souhaite garder par noeud, on prend généralement K=5 */

	/** The m path. */
	public int[]  m_path; /* Il s'agit du chemin parcouru, la liste a le même format que m_cities de la classe solution, on 
	recopie directement à l'issue du programme m_path dans m_cities */

	/** The m successor. */
	 public int[] m_successor;/*m_successor[City1] == City2 veut dire que dans le chemin actuel City1 précède City2*/

	/** The m predecessor. */
	public int[] m_predecessor;/*m_predecessor[City1] == City2 veut dire que dans le chemin actuel City1 succède à City2*/

	/** The m path indexed by cities. */
	public int[] m_path_indexed_by_cities;/** The m euclidean candidates. */
	/*m_path_indexed_by_cities[City1] == i veut dire que City1 est la 
	i+1ème ville visitée pour le chemin actuel*/
	
	/** The m K permutation. */
	public int[][] m_K_permutation;
	/** The nbCandidates */
	public int nbCandidates;
	/** The all_alpha_candidates */
	public int[][] all_alpha_candidates;
	
	
	/**Boolean qui permet de dire si l'on souhaite l'optimisation après l'éxecution du lkh*/
	public boolean OptimisationPost; 
	/*#########################################################################################################################*/
	/*-----------------------------------------------CONSTRUCTEUR & INITIALISATION---------------------------------------------*/
	/*----------------------------------------------(PREMIERE PARTIE DE L'ALGORITHME)------------------------------------------*/
	/*#########################################################################################################################*/

	/**
	 * Ce constructeur prend en entrée l'instance et construit les variables d'instances nécessaires au problème*.
	 *
	 * @param instance the instance
	 * @param name the name
	 * @throws Exception the exception
	 */


	public LKH_classic2(Instance instance, String name) throws Exception {
		super(instance, name);
		this.m_1_Tree= new int[this.m_instance.getNbCities()][this.m_instance.getNbCities()];
		this.m_dad = new int[this.m_instance.getNbCities()];
		this.m_special_node=-2; // initialisation de m_special_node négative pour debugging simplifié
		this.primAlgorithm();//On construit le 1-Tree, on crée ainsi this.m_dad et this.m_topological_prec_spanning_tree
		this.oneTreeOptimisation();//Optimisation du 1-tree pour obtenir le maximum de noeuds de degrés 2
		this.alpha_nearness();//Détermination de la matrice des distances alpha
		this.firstTourConstruction(this.candidates(this.m_alpha_nearness)); //Construction d'un premier tour en utilisant les calculs de alpha-nearness
		this.predecessor();//Détermine this.m_predecessor à partir du nouveau tour
		this.successor();//Détermine this.m_successor à partir du nouveau tour
		this.pathIndexedByCities();//Détermine this.m_path_indexed_by_cities à partir du nouveau tour
		this.OptimisationPost = false;
		if(this.OptimisationPost) {
		this.m_K_permutation=kPermutation();//Enregistre un ensemble de permutations
		}
		
	}
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*-------------------------------------------------PRIM ALGORITHME------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/**
	 * primAlgorithm est basé sur l'algorithme de PRIM pour former un Minimum Spanning-Tree et au final un 1-Tree.
	 * Le 1-Tree est stocké sous forme d'une matrice carré binaire this.m_1_Tree où this.m_1_Tree[i][j]==1 veut dire
	 * que les villes i et j sont reliées.
	 * STEP#1 On construit un minimum spanning-tree avec toutes les villes
	 * STEP#2 On construit une deuxième liaison avec l'une des villes pour former un 1-Tree. On choisit la ville qui à le 2ème plus proche voisin le plus éloigné.
	 * La valeur objective this.objectiveValue() de ce graphe permet d'obtenir une borne inférieure du problème du TSP.
	 * @throws Exception the exception
	 */

	public void primAlgorithm() throws Exception {
		int n=this.m_instance.getNbCities();
		if(this.m_1_Tree.length==0) { 
			return; //Impossible de former un 1-tree avec 0 villes
		}
		else {
			List<Integer> mstlist = new ArrayList<Integer>(); //Liste contenant les villes déjà ajoutée au spanning-tree
			List<Integer> cityLeft = new ArrayList<Integer>();//Liste contenant les villes restantes à ajouter
			for(int i=1; i<n;i++) {//on ajoute toutes les villes à la liste à l'exception de 0
				cityLeft.add(i);
			}
			mstlist.add(0); //0 est considéré comme déjà traité, choix arbitraire pratique
			while(mstlist.size()<n) {
			long d= Long.MAX_VALUE;
			int i=-1;
			int j=-1;
			for(int k : mstlist) { //On parcourt toutes les villes déjà traitées
				for(int l : cityLeft) {
					long dprime=this.m_instance.getDistances(k, l); /**On compare leurs distances avec toutes les villes
					non traitées*/
					if(d>dprime) {
						d=dprime;
						i=k;
						j=l;
					}
				}
				
			}
			mstlist.add(j); //On ajoute la ville la plus proche du spanning-tree au spanning-tree
			this.m_1_Tree[i][j]=1;
			cityLeft.remove((Object)j); //On enlève la ville de la liste des villes à traiter
			this.m_1_Tree[j][i]=1;
			
			}
			
			
			this.m_topological_prec_spanning_tree=mstlist;
			
			
			//Création de this.m_dad
			
					int[] tab = new int[n];
					tab[0]=-1;
					for(int i = 1; i<n; i++) {
						for(int j=i; j>=0; j--) {
							if(this.m_1_Tree[this.m_topological_prec_spanning_tree.get(j)][this.m_topological_prec_spanning_tree.get(i)]==1) {
								tab[this.m_topological_prec_spanning_tree.get(i)]=this.m_topological_prec_spanning_tree.get(j);
							}
							
							
						}
					}
					this.m_dad=tab;
					
			//Détermination du segment pour le plus long second plus proche voisin
					
			long[][] tab2 = new long[n][2];
			for(int i = 0; i<n; i++) {
				long d=Long.MAX_VALUE;
				int k=-1;
				for(int j=0; j<n; j++) {
					if(i!=j && this.m_1_Tree[i][j]!=1) {
						if(d>this.m_instance.getDistances(i, j)) {
							d=this.m_instance.getDistances(i, j);
							k=j;
							
						}
						
					}
					tab2[i][0]=k;
					tab2[i][1]=d;
				}
				
			}
			int max_index = maximum(tab2);
			this.m_1_Tree[max_index][(int) tab2[max_index][0]]= 1;
			this.m_1_Tree[(int) tab2[max_index][0]][max_index]= 1;
			this.m_special_node = max_index;
			
		}
	}

	

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*------------------------------------------OPTIMISATION DU 1-TREE -----------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * One tree optimisation.
	 *Si tous les noeuds de notre 1-Tree sont de degrés 2 alors nous avons un cycle minimal et le problème est résolu.
	 *On essaie d'optimiser le 1-Tree en "éloignant" virtuellement les villes de degrés trop haut et en rapprochant celle de
	 *degrés trop bas. On re-calcule le 1-Tree avec les nouvelles distances à l'aide de PrimAlgorithm2 qui fonctionne selon le
	 *même principe que PrimAlgorithm mais avec des distances virtuellement modifiés par el vecteur pi[].
	 *L'optimisation s'arrête selon des critères empiriques. 
	 * @throws Exception the exception
	 */
	public void oneTreeOptimisation() throws Exception {
		int n = this.m_1_Tree.length;
		long[] v1 = new long[n];
		long[] v2 = new long[n];
		long[] pi = new long[n];
		int c = 0;
		double t =100;											// peut etre changer t au profit d'un compromis avec les distances du problème
		for(int i=0; i<n; i++) { 								//peut etre faire une fonction de copie des vecteurs
			v1[i]=this.degree(i)-2;
			v2[i]=v1[i];
			pi[i]=(long)(v1[i]*t);
		}
		int period = n/2;
		long objectiveValue1= this.objectiveValue();
		long objectiveValue2=objectiveValue1;
		while(period!=0 && t>0 && c!=n ) {						//Critères d'arrêts empiriques
			for(int p=0; p<period; p++) {
			if(objectiveValue1==objectiveValue2) {
			t=t*2;
			//Pas de variation du poids augmenté si pas de variation notable du 1-Tree
			}
			else {
				objectiveValue1=-1;
				t=t/2; //Sinon le pas de variation du poids devient plus fin en le divisant par 2
				period = (int) period/2; 
				/*On diminue aussi la période par 2, c'est à dire le nombre de fois qu'on execute l'algorithme
				 sans vérifier els conditions d'arrêts*/
				
				
			}
			for(int i=0; i<n; i++) {
				double a=(pi[i]+v1[i]*t);//calcul de la valeur du vecteur poids "virtuel"
				pi[i]= (long) a;
				v2[i]=v1[i];
				
			}
				this.primAlgorithm(pi);//détermination du nouveau 1-Tree avec les poids "virtuels"
				c=0;
				for(int i=0; i<n; i++) {
					v1[i]=this.degree(i)-2; //Si le degré de la ville i est égal à 2, il n'ya pas de poids virtuel en v1[i]
					if(v1[i]==0) {
						c++;}
					}
				if(objectiveValue2<this.objectiveValue()) {
					period=period*4;
					t=t*2;
				}
				objectiveValue2=this.objectiveValue();
					
				
		}}
		return;
	}
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*-------------------------------------------------PRIM ALGORITHME II---------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Prim algorithm II .
	 *Ici on a des poids virtuels de part le vecteur pi, sinon même algorithme que précédemment.
	 * @param pi the pi
	 * @throws Exception the exception
	 */
	public void primAlgorithm(long[] pi) throws Exception {
		int resolution = 100;
		int n=this.m_instance.getNbCities();
		for(int i=0; i<n; i++) {
			for(int j= 0; j<n; j++) {
				this.m_1_Tree[i][j]=0;
			}
		}
		if(this.m_1_Tree.length==0) { 
			return; //Impossible de former un 1-tree avec 0 villes
		}
		else {
			List<Integer> mstlist = new ArrayList<Integer>(); //Liste contenant les villes déjà ajoutée au spanning-tree
			List<Integer> cityLeft = new ArrayList<Integer>();//Liste contenant les villes restantes à ajouter
			for(int i=1; i<n;i++) {//on ajoute toutes les villes à la liste à l'exception de 0
				cityLeft.add(i);
			}
			mstlist.add(0); //0 est considéré comme déjà traité, choix arbitraire pratique
			while(mstlist.size()<n) {
			long d= Long.MAX_VALUE;
			int i=-1;
			int j=-1;
			for(int k : mstlist) { //On parcourt toutes les villes déjà traitées
				for(int l : cityLeft) {
					long dprime=resolution*this.m_instance.getDistances(k, l) + pi[k] + pi[l]; /**On compare leurs distances avec toutes les villes
					non traitées*/
					if(d>dprime) {
						d=dprime;
						i=k;
						j=l;
					}
					
				}
				
			}
			mstlist.add(j); //On ajoute la ville la plus proche du spanning-tree au spanning-tree
			this.m_1_Tree[i][j]=1;
			cityLeft.remove((Object)j); //On enlève la ville de la liste des villes à traiter
			this.m_1_Tree[j][i]=1;
			
			}
			
			
			this.m_topological_prec_spanning_tree=mstlist;
			//Création de this.m_dad
					int[] tab = new int[n];
					tab[0]=-1;
					for(int i = 1; i<n; i++) {
						for(int j=i; j>=0; j--) {
							if(this.m_1_Tree[this.m_topological_prec_spanning_tree.get(j)][this.m_topological_prec_spanning_tree.get(i)]==1) {
								tab[this.m_topological_prec_spanning_tree.get(i)]=this.m_topological_prec_spanning_tree.get(j);
							}
							
							
						}
					}
					////System.out.println(Arrays.deepToString(this.m_1_Tree));
					this.m_dad=tab;
			//Détermination du segment pour le plus long second plus proche voisin
			long[][] tab2 = new long[n][2];
			for(int i = 0; i<n; i++) {
				long d=Long.MAX_VALUE;
				int k=-1;
				for(int j=0; j<n; j++) {
					if(i!=j && this.m_1_Tree[i][j]!=1) {
						if(d>resolution*this.m_instance.getDistances(i, j)+pi[i]+pi[j]) {
							d=resolution*this.m_instance.getDistances(i, j)+pi[i]+pi[j];
							k=j;
							
						}
						
					}
					tab2[i][0]=k;
					tab2[i][1]=d;
				}
				
			}
			int max_index = maximum(tab2);
			this.m_1_Tree[max_index][(int) tab2[max_index][0]]= 1;
			this.m_1_Tree[(int) tab2[max_index][0]][max_index]= 1;
			this.m_special_node = max_index;
			
		}
	}
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*--------------------------------------------------BETA NEARNESS-------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Beta nearness.
	 *Le calcul de la "Beta nearness" est intermediaire au calcul de "l'alpha nearness".
	 *La matrice beta retournée donne la longueur beta[i][j] de la liaison minimale à casser
	 *pour conserver un 1-Tree après la création de la liaison entre la ville i et j.
	 * @return the long[][]
	 * @throws Exception the exception	
	 */
	public long[][] beta_nearness() throws Exception {
		int n = this.m_1_Tree.length;
		long[][] beta = new long[n][n];
		for(int i=0; i<n;i++) {
			beta[this.m_topological_prec_spanning_tree.get(i)][this.m_topological_prec_spanning_tree.get(i)]=Long.MIN_VALUE;
			for(int j=i+1;j<n;j++) {
				if(i!=j && this.m_topological_prec_spanning_tree.get(i)!=this.m_special_node && this.m_topological_prec_spanning_tree.get(j)!=this.m_special_node) {
				int k = this.m_topological_prec_spanning_tree.get(i);
				int l = this.m_topological_prec_spanning_tree.get(j);
				beta[k][l]=Math.max(beta[k][this.m_dad[l]],this.m_instance.getDistances(l, this.m_dad[l]));
				beta[l][k]=beta[k][l];}
				else if(i!=j && (this.m_topological_prec_spanning_tree.get(i)!=this.m_special_node||this.m_topological_prec_spanning_tree.get(j)!=this.m_special_node)) {
					int k=-1;
					int l=-1;
					for(int w=0; w<this.m_topological_prec_spanning_tree.size(); w++) {
						if(this.m_1_Tree[this.m_special_node][w]==1 && k==-1) {
							k=w;
						}
						if(this.m_1_Tree[this.m_special_node][w]==1 && k!=-1) {
							l=w;
						}
					}
					beta[this.m_topological_prec_spanning_tree.get(i)][this.m_topological_prec_spanning_tree.get(j)]=
							Math.min(this.m_instance.getDistances(this.m_special_node, k),this.m_instance.getDistances(this.m_special_node, l));
					beta[this.m_topological_prec_spanning_tree.get(j)][this.m_topological_prec_spanning_tree.get(i)]=beta[this.m_topological_prec_spanning_tree.get(i)][this.m_topological_prec_spanning_tree.get(j)];
				}
			}
		
		}
		return beta;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*---------------------------------------------ALPHA-NEARNESS-----------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Alpha nearness.
	 *L'alpha nearness se calcule à partir de la beta nearness et correspond au coût d'ajout de la liaison i--j 
	 *dans le 1-Tree. Plus cette valeur est grande moins l'on considère la liaison comme probable par la suite.
	 * @return the long[][]
	 * @throws Exception the exception
	 */
	public long[][] alpha_nearness() throws Exception{
		long [][] beta = this.beta_nearness();
		int n = beta.length;
		long[][] alpha = new long[n][n];
		for(int i=0; i<n; i++) {
			for(int j=i; j<n; j++) {
				alpha[i][j]= this.m_instance.getDistances(i, j)-beta[i][j];
				alpha[j][i]=alpha[i][j];
			}
		}
		this.m_alpha_nearness=alpha;
		return alpha;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*------------------------------------CONSTRUCTION DU PREMIER TOUR------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * First tour construction.
	 *Pour chaque ville on possède par ordre décroissant de probabilité (selon le critère d'alpha nearness) les villes aux-quelles
	 *elle pourrait être reliée. 
	 *On construit un premier tour en prenant pour chaque ville la ville la plus probable encore disponible.
	 *Le résultat est meilleur qu'un résultat aléatoire et qu'un "plus proche voisin" et peu coûteux puisque le calcul
	 *des candidats à déjà été fait pour l'amélioration ultérieure du tour.
	 * @param candidates the candidates
	 * @return the int[]
	 * @throws Exception the exception
	 */
	public int[] firstTourConstruction(int[][] candidates) throws Exception {
		int n= this.m_instance.getNbCities();
		int k= candidates[0].length;
		ArrayList<Integer> LeftCities = new ArrayList<Integer>();
		ArrayList<Integer> AlreadySetCities = new ArrayList<Integer>();
		for(int w=1; w<n; w++) {
			LeftCities.add(w);
		}
		AlreadySetCities.add(0);
		for(int j=0; j<n; j++) {
			int c1=0;
			int c2=0;
			while(c1==0 && c2<k) {
				int a = LeftCities.indexOf(candidates[j][c2]);
			if(a!=-1) {
				c1++;
				AlreadySetCities.add(candidates[j][c2]);
				LeftCities.remove(a);
			}
			else {
				c2++;
			}
				
			}
			if(c2==k && LeftCities.size()!=0) {
				AlreadySetCities.add(LeftCities.get(0));
				LeftCities.remove(0);
			}
		}
		AlreadySetCities.add(0);
		int[] liste = new int[n+1];
		for(int w=0; w<n+1; w++) {
			liste[w]= AlreadySetCities.get(w);
		}
		this.m_path=liste;
		return liste;
	}


	/*----------------------------------------------------------------------------------------------------------------------*/
	/*--------------------------------------AUTRES REPRESENTATIONS DU TOUR---------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	
	//#####-1-#### Tour Indexé par numéro de villes
	
	/**
	 * Path indexed by cities.
	 *Liste indexée par numéro de ville, this.m_path_indexed_by_cities[i] donne le rang auquel on visite la ième ville
	 *sur le chemin.
	 * @return the int[]
	 */
	public int[] pathIndexedByCities() {
		int n = this.m_instance.getNbCities();
		this.m_path_indexed_by_cities = new int[n];
		for(int i=0;i<n; i++) {
			this.m_path_indexed_by_cities[this.m_path[i]]=i;
		}
		return this.m_path_indexed_by_cities;
	}
	
	public int[] pathIndexedByCities(int index1, int index2) {
		for(int i=index1;i<index2+1; i++) {
			this.m_path_indexed_by_cities[this.m_path[i]]=i;
		}
		return this.m_path_indexed_by_cities;
	}

	//#####-2-#### Liste du predecesseur de chaque ville

	/**
	 * Predecessor.
	 *this.m_predecessor[i] donne le predecesseur de la ville i sur le chemin parcouru.
	 *remarque: le predecesseur de la première ville visitée est la dernière ville visitée.
	 * @return the int[]
	 */
	public int[] predecessor() {
		int n = this.m_instance.getNbCities();
		this.m_predecessor=new int[n];
		this.m_predecessor[0]=this.m_path[n];
		for(int k=1; k<n+1;k++ ) {
			this.m_predecessor[this.m_path[k]]=this.m_path[k-1];
		}
		return this.m_predecessor;
			
	}
	public int[] predecessor(int index1, int index2) {
		for(int k=index1; k<index2+2;k++ ) {
			this.m_predecessor[this.m_path[k]]=this.m_path[k-1];
		}
		return this.m_predecessor;
			
	}

	//#####-3-#### Liste du successeur de chaque ville
	/**
	 * Successor.
	 * *this.m_successor[i] donne le successeur de la ville i sur le chemin parcouru.
	 *remarque: le successeur de la dernière ville visitée est la première ville visitée.
	 * @return the int[]
	 */
	public int[] successor() {
		int n = this.m_instance.getNbCities();
		this.m_successor=new int[n];
		this.m_successor[0]=this.m_path[1];
		for(int k=0; k<n;k++ ) {
			this.m_successor[this.m_path[k]]=this.m_path[k+1];
		}
		return this.m_successor;
			
	}
	public int[] successor(int index1, int index2) {
		for(int k=index1-1; k<index2+1;k++ ) {
			this.m_successor[this.m_path[k]]=this.m_path[k+1];
		}
		return this.m_successor;
			
	}


	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------RELATION DE SUPERIORITE-------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/**
	 * Superieur.
	 * Si deux alpha-nearness sont égales, c'est la ville qui est la plus proches physiquement qui est la plus probable
	 * @param a the a
	 * @param b the b
	 * @param i the i
	 * @param j1 the j 1
	 * @param j2 the j 2
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean superieur(long a, long b, int i, int j1, int j2) throws Exception {
		if(a>b) {
			return true;
		}
		else if(a==b) {
			if(this.m_instance.getDistances(i, j1)>this.m_instance.getDistances(i, j2)) {
				return true;
			}
		}
		return false;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*-----------------------------------------CONSTRUCTION DU TABLEAU DES CANDIDATS----------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Candidates.
	 *Construit les candidats pour chaque ville
	 * @param alpha_nearness the alpha nearness
	 * @return the int[][]
	 * @throws Exception the exception
	 */
	public int[][] candidates(long[][] alpha_nearness) throws Exception{
		int n = this.m_instance.getNbCities();
		int k =n-1;
		int[][] candidates = new int[n][k];
		int[] temp = new int[k];
		for(int i=0; i<n; i++) {
			for(int w=0; w<k;w++) {
				temp[w]= Integer.MAX_VALUE;
			}
			for(int j=0; j<n; j++) {
				if(i!=j) {
					int c1=0;
					int c2=0;
					while(c1==0&&c2<k) {
						if(temp[c2]==Integer.MAX_VALUE||this.superieur(alpha_nearness[temp[c2]][i], alpha_nearness[i][j], i, temp[c2], j)) {
							c1=1;
							for(int w=k-1; w>c2; w=w-1) {
								temp[w]=temp[w-1];
							}
							temp[c2]=j;
						}
						else {
							c2++;
						}
					}
					
				}
			}
			for(int w=0; w<k; w++) {
				candidates[i][w]=temp[w];
			}
			
		}
		this.nbCandidates=5;
		int nbCandidates =this.nbCandidates;
		int[][] alpha_candidates = new int[n][nbCandidates];
		for(int g = 0 ; g<n; g++) {
			for(int j =0; j<nbCandidates;j++) {
				alpha_candidates[g][j]=candidates[g][j];
			}
		}
		this.alpha_candidates=alpha_candidates;
		this.all_alpha_candidates=candidates;
		return candidates;
		
		
	}
	
	/*/**
	 * Candidates.
	 *
	 * @throws Exception the exception
	 
	public void candidates() throws Exception{
		int n = this.m_instance.getNbCities();
		int nbCandidates =this.nbCandidates;
		int[][] alpha_candidates = new int[n][nbCandidates];
		for(int g = 0 ; g<n; g++) {
			for(int j =0; j<nbCandidates;j++) {
				alpha_candidates[g][j]=this.all_alpha_candidates[g][j];
			}
		}
		this.alpha_candidates=alpha_candidates;
	}*/


	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/*#########################################################################################################################*/
	/*-----------------------------------------------AMELIORATION DU TOUR------------------------------------------------------*/
	/*#########################################################################################################################*/

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------SOLVE-------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/* (non-Javadoc)
	 * @see tsp.heuristic.AHeuristic#solve()
	 */
	/*Dans un premier temps on applique un algorithme de type lkh avec les alpha candidats les plus proches
	 * l'algorithme étant très rapide avec les tailles d'instances proposées (moins de 9 secondes pour d657.tsp), 
	 * on décide d'optimiser le tour obtenu, d'abord on retente une amélioration par un lkh basé sur les plus proches voisins
	 * (valable car nos alpha candidats sont basés sur un 1-Tree qui n'est lui même pas opitmal, on trouve quelques améliorations sur les
	 * 15 plus proches voisins)
	 * S'il reste du temps on améliore avec un algorithme de grande complexité qui test toutes les permutations parmis 9 liaisons consécutives
	 * On trouve des améliorations là encore bien que l'efficacité soit limitée*/
	
	public void solve() throws Exception {
		
		int n = this.m_instance.getNbCities();
		long startTime = System.currentTimeMillis();
		int[][] tab = this.alpha_candidates;
		int limit=1;
	
		if(this.OptimisationPost) {
			limit=2;
		}
		for(int k=0; k<limit;k++) {
		boolean FourOpt= false;
		boolean FiveOpt= false;
		long length=this.objectiveValuePath()+1;
		while(FourOpt==false||FiveOpt==false||length!=this.objectiveValuePath()) {
			
			if(length==this.objectiveValuePath()) {
				if(FourOpt==false) {
				FourOpt=true;
				}
				else {
					FiveOpt=true;
				}
			}
			length=this.objectiveValuePath();
		for(int t1=0;t1<n;t1++) {
				this.lkhMove(t1,FourOpt,FiveOpt,tab);
			}
		if(this.m_instance.getNbCities()<20 && FourOpt==true) {
			break;
		}
			}
		if(this.m_instance.getNbCities()>20) {

		if(this.OptimisationPost) {tab=this.euclideanCandidatesGenerator();
		}
		}
			
		}
		if(this.m_instance.getNbCities()>10 && this.OptimisationPost) {
		this.successor();
		this.pathIndexedByCities();
		for(int w = 0; w<this.m_instance.getNbCities()-11; w++) {
			if(System.currentTimeMillis() - startTime>57000) {
				for(int i=0; i<this.m_path.length;i++) {
					this.m_solution.setCityPosition(this.m_path[i], i);
						}
				return;
			}
			
			this.permutationOpti(this.m_path[w]);
			
		}}
		for(int w = 0; w<this.m_instance.getNbCities(); w++) {
			if(System.currentTimeMillis() - startTime>57000) {
				for(int i=0; i<this.m_path.length;i++) {
					this.m_solution.setCityPosition(this.m_path[i], i);
						}
				
			}}

		for(int i=0; i<this.m_path.length;i++) {
			this.m_solution.setCityPosition(this.m_path[i], i);
				}

		return ;
		}

				
		
		
		
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*---------------------------------------------LIN KHERNIGHAN MOVE------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * Lkh move.
	 *
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @param fourOpt the four opt
	 * @param fiveOpt the five opt
	 * @param alpha_candidates the alpha candidates
	 * @throws Exception the exception
	 * Pour une ville donnée t1, va tenter d'améliorer le tour d'abord par un 2-opt puis 3-opt et si disponible 4 ou 5-opt.
	 * Expérimentalement on a observé qu'autoriser dès le départ les mouvement 4 et 5 opt conduisait à des moins bon résultats
	 */
	public void lkhMove(int t1 , boolean fourOpt, boolean fiveOpt, int[][] alpha_candidates) throws Exception {
	List<Integer> liste = new ArrayList<Integer>();
	liste.add(t1);
	for(int x2=1; x2<3;x2++) {//On construit un t2 qui correspond à une ville déjà reliée à t1
		int t2 = (x2==1)? this.m_successor[t1] : this.m_predecessor[t1];
		liste.add(t2);
	long G0=this.m_instance.getDistances(t1, t2);
	for(int k1 = 0; k1< alpha_candidates[t2].length; k1++) {
		int t3 = alpha_candidates[t2][k1];
		long G1=G0-this.m_instance.getDistances(t2, t3);
		if(t3==this.m_predecessor[t2]||t3==this.m_successor[t2]||G1<=0) { //Si t3 est successeur ou predecesseur de t2 aucun interêt de regarder si on peut les relier.
			continue;
		}
		liste.add(t3);
		for(int x4=1; x4<3;x4++) { //On construit un t4 qui correspond à une ville déjà liée à t3
			int t4 = (x4==1)? this.m_successor[t3] : this.m_predecessor[t3];
			long Gain = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)-this.m_instance.getDistances(t1, t4)-this.m_instance.getDistances(t2, t3);
			if(Gain>0 && t2!=t4 && x2!=x4) { 
	        /*Si il y'a un gain de distance à casser t1-t2 et t3-t4 pour former t1-t4 et t2-t3 on le fait 
	         * (seulement si on ne forme pas 2 cycles distincts)*/
				int c=0;
				c=this.make2Opt(t1, t2, t3, t4);
				if(c!=-1) { //c==-1 si le swap résulte en la formation de deux cycles distincts, on continue alors vers le 3-opt etc...jusqu'au 5-opt
					return;
				}
				
			}
			else {liste.add(t4);
				for(int k2 = 0; k2< alpha_candidates[t4].length; k2++) {
					int t5 = alpha_candidates[t4][k2];
				if(t5==this.m_predecessor[t4]||t5==this.m_successor[t4]||liste.contains(t5)) {
					continue;
				}
				liste.add(t5);
				for(int x6=1; x6<3;x6++) {
					int t6 = (x6==1)? this.m_successor[t5] : this.m_predecessor[t5];
					if(liste.contains(t6)) {
						continue;
					}
					liste.add(t6);
					boolean valid = this.make3Opt(t1,t2,t3,t4,t5,t6);
					if(valid==true) {
						return;
						
					}
					if(fourOpt==true) {
					for(int k3 = 0; k3<alpha_candidates[t6].length; k3++) {
						int t7 = alpha_candidates[t6][k3];
						if(t7==this.m_predecessor[t6]||t7==this.m_successor[t6]||liste.contains(t7)) {
							continue;
						}
						liste.add(t7);
						for(int x8=1; x8<3;x8++) {
							int t8 = (x8==1)? this.m_successor[t7] : this.m_predecessor[t7];
							if(liste.contains(t8)) {
								continue;
							}
							liste.add(t8);
							boolean valid2 = this.make4Opt(t1,t2,t3,t4,t5,t6,t7,t8);
							if(valid2==true) {
								return;
							}
							if(fiveOpt==true) {
								for(int k4 = 0; k4<alpha_candidates[t8].length; k4++) {
									int t9 = alpha_candidates[t8][k4];
									if(t9==this.m_predecessor[t8]||t9==this.m_successor[t8]||liste.contains(t9)) {
										continue;
									}
									liste.add(t9);
									for(int x10=1; x10<3;x10++) {
										int t10 = (x10==1)? this.m_successor[t9] : this.m_predecessor[t9];
										if(liste.contains(t10)) {
											continue;
										}
										liste.add(t10);
										boolean valid3 = this.make5Opt(t1,t2,t3,t4,t5,t6,t7,t8,t9,t10);
										if(valid3==true) {
											return;
										}
							liste.remove((Object) t10);}
								liste.remove((Object) t9);}
							
							}liste.remove((Object) t8);}
					liste.remove((Object) t7);	
					
			}}liste.remove((Object) t6);
				
			
			}liste.remove((Object) t5);
		
	}}liste.remove((Object) t4);
		}liste.remove((Object) t3);

	}liste.remove((Object) t2);}
	return;}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*------------------------------------------LE SWAP BRIQUE ELEMENTAIRE--------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * Swap.
	 * 
	 * @param i the i
	 * @param j the j
	 * Prend deux villes i et j, réalise leurs swap au sein du parcours this.m_path
	 */
	public void swap(int i, int j) {
	int first = (this.m_path_indexed_by_cities[i]<this.m_path_indexed_by_cities[j])? i : j;
	int last = (this.m_path_indexed_by_cities[i]<this.m_path_indexed_by_cities[j])? j : i;
	int index1 = this.m_path_indexed_by_cities[first];
	int index2 = this.m_path_indexed_by_cities[last];
	if(index1!=0 && index2!=0) {
		int nbSwaps = (int)((this.m_path_indexed_by_cities[last]-this.m_path_indexed_by_cities[first]+1)/2);
		int indexfirst=this.m_path_indexed_by_cities[first];
		int indexlast=this.m_path_indexed_by_cities[last];
		for(int w=0; w<nbSwaps; w++) {
			int a = this.m_path[indexfirst+w];
			this.m_path[indexfirst+w]=this.m_path[indexlast-w];
			this.m_path[indexlast-w]=a;
		}
	}
	else {
		this.swap(index2+1, this.m_path.length-2);
		return;
		}
	this.successor(index1,index2);						/*Il y'a un manque d'optimisation reconnu dans le recalcul des listes complètes à chaque swap
	il faudrait indexer les endroits à modifier*/
	this.predecessor(index1,index2);
	this.pathIndexedByCities(index1,index2);
	}
	
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*------------------------------------LA DEUXIEME BRIQUE 2-OPT----------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	
	
	/**
	 * Make 2 opt.
	 * Réalise une amélioration de type 2-opt si possible, c'est à dire 
	 * si la suppression de t1--t2 et t3--t4 et la réalisation de t1--t4 et t3--t2 ne forme pas deux cycles distincts
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @param t3 the t 3
	 * @param t4 the t 4
	 * @return the int
	 * Retourne -1 si n'a rien modifié au tour
	 */
	public int make2Opt(int t1, int t2,int t3,int t4) {
		int x2;
		int x4;
		if(this.m_successor[t1]==t2) {
			x2=1;
		}
		else {
			x2=2;
		}
		if(this.m_successor[t3]==t4) {
			x4=1;
		}
		else {
			x4=2;
		}
		if((this.m_successor[t1]!=t2 && this.m_successor[t2]!=t1)||(this.m_successor[t3]!=t4 && this.m_successor[t4]!=t3)) {
			return -1;
		}
		if(x2!=x4) {
		if((this.m_path_indexed_by_cities[t2]>this.m_path_indexed_by_cities[t3] && x2!=1)||(this.m_path_indexed_by_cities[t1]<this.m_path_indexed_by_cities[t4] && x2==1)) {
			this.swap(t2, t4);
			return 1;
		}
		else {
			this.swap(t1, t3);
			return 2;
		}}
		else {
			return -1;
		}
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Make 5 opt.
	 *
	 *
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @param t3 the t 3
	 * @param t4 the t 4
	 * @param t5 the t 5
	 * @param t6 the t 6
	 * @param t7 the t 7
	 * @param t8 the t 8
	 * @param t9 the t 9
	 * @param t10 the t 10
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	private boolean make5Opt(int t1, int t2, int t3, int t4, int t5, int t6, int t7, int t8, int t9, int t10) throws Exception {
		long Obj = this.objectiveValuePath();
		long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6)+this.m_instance.getDistances(t7,t8)+this.m_instance.getDistances(t9, 10);
		long G1= G0-this.m_instance.getDistances(t1, t9)-this.m_instance.getDistances(t2, t3)-this.m_instance.getDistances(t4,t5)-this.m_instance.getDistances(t6, t7)-this.m_instance.getDistances(t8, t9);
		
		if(G1>0) {
			this.make2Opt(t1,t2,t9,t10);
			this.make2Opt(t3,t4,t9,t2);
			this.make2Opt(t5,t6,t9,t4);
			this.make2Opt(t7,t8,t9,t6);
		if(Obj<this.objectiveValuePath()) {
			this.make2Opt(t7,t6,t9,t8);
			this.make2Opt(t5,t4,t9,t6);
			this.make2Opt(t3,t2,t9,t4);
			this.make2Opt(t1,t10,t9,t2);
				return false;
		}}
		else if (G1<=0){
			return false;}
		return true;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * Make 3 opt.
	 *
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @param t3 the t 3
	 * @param t4 the t 4
	 * @param t5 the t 5
	 * @param t6 the t 6
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public boolean make3Opt(int t1, int t2, int t3, int t4, int t5, int t6) throws Exception {
		long Obj = this.objectiveValuePath();
		long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6);
		long G1= G0-this.m_instance.getDistances(t1, t6)-this.m_instance.getDistances(t2, t3)-this.m_instance.getDistances(t4,t5);
		if(G1>0) {
			this.make2Opt(t1,t2,t5,t6);
			this.make2Opt(t5,t2,t3,t4);
		if(Obj<this.objectiveValuePath()) {
			this.make2Opt(t5,t4,t3,t2);
			this.make2Opt(t1,t6,t5,t2);
			return false;
			
		}}
		else if (G1<=0){
			return false;}
		return true;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Make 4 opt.
	 *
	 * @param t1 the t 1
	 * @param t2 the t 2
	 * @param t3 the t 3
	 * @param t4 the t 4
	 * @param t5 the t 5
	 * @param t6 the t 6
	 * @param t7 the t 7
	 * @param t8 the t 8
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	public boolean make4Opt(int t1,int t2,int t3,int t4,int t5,int t6,int t7,int t8) throws Exception {
		long Obj = this.objectiveValuePath();
		long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6)+this.m_instance.getDistances(t7,t8);
		long G1= G0-this.m_instance.getDistances(t1, t8)-this.m_instance.getDistances(t2, t3)-this.m_instance.getDistances(t4,t5)-this.m_instance.getDistances(t6, t7);
		if(G1>0) {
			this.make2Opt(t1,t2,t7,t8);
			this.make2Opt(t3,t4,t7,t2);
			this.make2Opt(t5,t6,t7,t4);

		if(Obj<this.objectiveValuePath()) {
			this.make2Opt(t5, t4, t7, t6);
			this.make2Opt(t3, t2, t7, t4);
			this.make2Opt(t1, t8, t7, t2);
				return false;}

		}
		else if (G1<=0){
			return false;}
		return true;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*##############################################################################################################################*/
	/*----------------------------------AMELIORATION DU TOUR PENDANT LE TEMPS RESTANT----------------------------------------------*/
	/*############################################################################################################################*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	
	
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------PERMUTONS, PERMUTONS !--------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/**
	 * Permutation opti.
	 *Realise toutes les permutations possible dans l'ordre de visite des 9 villes suivantes
	 * @param t1 the t 1
	 * @throws Exception the exception
	 */
	public void permutationOpti(int t1) throws Exception {
		int Koef = 11;
		int[] liste = new int[Koef];
		liste[0]=t1;
		int min =-1;
		for(int i = 1; i<Koef;i++) {
			liste[i]=this.m_successor[liste[i-1]];
		}
		int index1=this.m_path_indexed_by_cities[t1];
		int index2=this.m_path_indexed_by_cities[liste[Koef-1]];
		if(index1>index2) {
			this.successor(1,index2);
			this.successor(index1,this.m_instance.getNbCities()-1);
			this.pathIndexedByCities(0, index2);
			this.pathIndexedByCities(index1, this.m_instance.getNbCities()-1);
			
		}
		else {
			this.successor(index1+1,index2);
			this.pathIndexedByCities(index1, index2);
		}
		long dmin =0;
		for(int g=0; g<Koef-1;g++) {
			dmin+=this.m_instance.getDistances(liste[g], liste[g+1]);
		}
		for(int k = 0 ; k<this.m_K_permutation.length;k++) {
			long d=0;
			d+=this.m_instance.getDistances(liste[0], liste[this.m_K_permutation[k][0]]);
			d+=this.m_instance.getDistances(liste[Koef-1], liste[this.m_K_permutation[k][Koef-3]]);
			for(int w=0; w<Koef-3; w++) {
				d+=this.m_instance.getDistances(liste[this.m_K_permutation[k][w]], liste[this.m_K_permutation[k][w+1]]);
			}
			if(d<dmin) {
				dmin=d;
				min=k;
			}
		}
		int startIndex=this.m_path_indexed_by_cities[t1];
		if(min!=-1) {
			//String str1 = "";
			//String str2 = "";
		for(int i=1; i<Koef-1;i++) {
		//this.swap(liste[i], liste[this.m_K_permutation[min][i-1]]);
			//str1+=" "+this.m_path[startIndex+i];
			//str2+=" "+liste[this.m_K_permutation[min][i-1]];
			this.m_path[startIndex+i] = liste[this.m_K_permutation[min][i-1]];
			//this.m_path[this.m_path_indexed_by_cities[liste[0]]+i]=liste[i];
			
		}
		//System.out.println(str1);
		//System.out.println(str2);
		}
	}



	/**
	 * Factorielle.
	 *
	 * @param k the k
	 * @return the int
	 */
	private static int factorielle(int k) {
		if(k==1) {
			return 1;
		}
		else {
			return k*factorielle(k-1);
		}
	}

	/**
	 * Kpermutation.
	 *
	 * @return the int[][]
	 * Crée un tableau avec toutes les permutations existantes pour k objets
	 */ 
	private static int[][] kPermutation() {
		int k =9;//k<10
		ArrayList<String> tab = new ArrayList<String>();
		int[][] tab2 = new int[factorielle(k)][k];
		String str = "";
		for(int i = 1; i<k+1;i++) {
			char a = (char) (i + '0');
			str+= a;
		}
		permutation(str,tab);
		for(int j=0; j<tab.size();j++) {
			for(int w=0;w<k;w++) {
				String s = tab.get(j);
				char c = s.charAt(w);
				tab2[j][w]=c-'0';
			}
		}
		return tab2;
	}

	/**
	 * Permutation.
	 *
	 * @param str the str
	 * @param tab the tab
	 */
	public static void permutation(String str, ArrayList<String> tab) { 
	    permutation("", str, tab); 
	}

	/**
	 * Permutation.
	 *
	 * @param prefix the prefix
	 * @param str the str
	 * @param array the array
	 */
	private static void permutation(String prefix, String str, ArrayList<String> array) {
	    int n = str.length();
	    if (n == 0) array.add(prefix);
	    else {
	        for (int i = 0; i < n; i++)
	            permutation(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n), array);
	    }
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------RETOUR SUR LE PLUS PROCHE VOISIN----------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/**
	 * Euclidean candidates generator.
	 *On génère une liste de candidats avec les  15 voisins les plus proches physiquement de la ville comme candidats
	 * @throws Exception the exception
	 */
	public int[][] euclideanCandidatesGenerator() throws Exception {
		int n = this.m_instance.getNbCities();
		int k =n-1;
		int[][] candidates = new int[n][k];
		int[] temp = new int[k];
		for(int i=0; i<n; i++) {
			for(int w=0; w<k;w++) {
				temp[w]= Integer.MAX_VALUE;
			}
			for(int j=0; j<n; j++) {
				if(i!=j) {
					int c1=0;
					int c2=0;
					while(c1==0&&c2<k) {
						if(temp[c2]==Integer.MAX_VALUE||this.m_instance.getDistances(temp[c2],i)> this.m_instance.getDistances(i, j)) {
							c1=1;
							for(int w=k-1; w>c2; w=w-1) {
								temp[w]=temp[w-1];
							}
							temp[c2]=j;
						}
						else {
							c2++;
						}
					}
					
				}
			}
			for(int w=0; w<k; w++) {
				candidates[i][w]=temp[w];
			}
			
		}
		int nbCandidatesEucli=15;
		int[][] Eucli_candidates = new int[n][nbCandidatesEucli];
		for(int g = 0 ; g<n; g++) {
			for(int j =0; j<nbCandidatesEucli;j++) {
				Eucli_candidates[g][j]=candidates[g][j];
			}
		}
		return Eucli_candidates;
		
		
	}
/*
	public void mergePath(int[] path1, int[] path2, int[] successeur1, int[] successeur2) {
		int n=this.m_instance.getNbCities();
		int[] path = new int[n+1];
		ArrayList<Integer> listeLiaisonsCommunes = new ArrayList<Integer>();
		ArrayList<Integer> listeLiaisonsNonCommunes = new ArrayList<Integer>();
		if(successeur1[0]==successeur2[0]) {
			listeLiaisonsCommunes.add(0);
		}
		for(int i=0; i<n;i++) {
			if(successeur1[i]==successeur2[i]){
				listeLiaisonsCommunes.add(i);
			}
			if(successeur1[i]==successeur2[i] && successeur1[successeur1[i]]!=successeur2[successeur2[i]]) {
				listeLiaisonsNonCommunes.add(i);
			}
		}
		System.out.println(Arrays.toString(path1));
		 System.out.println(Arrays.toString(path2));
		 System.out.println(Arrays.toString(listeLiaisonsCommunes.toArray()));
		 System.out.println(Arrays.toString(listeLiaisonsNonCommunes.toArray()));
		
		
		
	}*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	
	/*##############################################################################################################################*/
	/*----------------------------------------------------FONCTIONS BASIQUES-------------------------------------------------------*/
	/*############################################################################################################################*/
	
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/**
	 * Objective value.
	 *Donne la distance totale pour parcourir le 1-Tree, donne une borne inf de la longueur du tour
	 * @return the long
	 * @throws Exception the exception
	 */
	public long objectiveValue() throws Exception {
		int n = this.m_1_Tree.length;
		long objectiveValue=0;
		for(int i=0; i<n ;i++) {
			for(int j=i; j<n;j++) {
				if(this.m_1_Tree[i][j]!=0) {
				objectiveValue+=this.m_instance.getDistances(i, j);
			}}
			
			}
		return objectiveValue;}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Objective value path.
	 *Donne la distance du tour actuel, il faudrait au vue de la fréquence à laquelle est appelée la fonction optimiser 
	 *pour ne pas avoir à tout recalculer à chaque fois.
	 * @return the long
	 * @throws Exception the exception
	 */
	public long objectiveValuePath() throws Exception {
		int[] path = this.m_path;
		int n = path.length;
		long objectiveValue=0;
		for(int i=0; i<n ;i++) {
			objectiveValue+=this.m_instance.getDistances(path[i], path[(i+1)%n]);
			}
			
		return objectiveValue;}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Degree moyen.
	 *Donne le degré moyen de toutes les villes
	 * @return the double
	 */
	public double degreeMoyen() {
		int n= this.m_instance.getNbCities();
		double sum=0;
		for(int i=0; i<n; i++) {
			sum+=this.degree(i);
		}
		return sum/n;
	}
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/

	/**
	 * Nombre degree deux.
	 * Donne l'effectif suivant le degré du 1-Tree 
	 */
	public void nombreDegreeDeux() {
		int n= this.m_instance.getNbCities();
		int deg1=0;
		int deg2=0;
		int deg3=0;
		int degplus=0;
		for(int i=0; i<n; i++) {
			switch(this.degree(i)) {
			case 1 : deg1++;
			break;
			case 2 : deg2++;
			break;
			case 3 : deg3++;
			break;
			default : degplus++;
			break;
			}
		}
		System.out.println("deg1234+: "+ deg1 +" "+ deg2 +" "+deg3+" "+ degplus);
		
	}

	

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * Maximum.
	 *Fonction permettant de trouver la ville avec le second plus proche voisin le plus éloigné
	 * @param tab the tab
	 * @return the int
	 */
	private static int maximum(long[][] tab) {
		
		int size= tab.length;
		long d=Long.MIN_VALUE;
		int j= -1;
		for(int i=0; i<size; i++) {
			if(tab[i][1]>d) {
				d=tab[i][1];
				j=i;
			}
		}
		
		return j;
	}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/


	/**
	 * To int array.
	 *Transforme une liste en array
	 * @param list the list
	 * @return the int[]
	 */
	public int[] toIntArray(List<Integer> list){
		  int[] ret = new int[list.size()];
		  for(int i = 0;i < ret.length;i++)
		    ret[i] = list.get(i);
		  return ret;
		}

	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/
	/*----------------------------------------------------------------------------------------------------------------------*/




	/**
	 * Degree.
	 *Donne le degré de la ville i dans le 1-Tree
	 * @param cityNumber the city number
	 * @return the int
	 */
	public int degree(int cityNumber) {
		int degree = 0;
		int l= this.m_1_Tree.length;
		for(int i=0; i<l; i++) {
			if(this.m_1_Tree[cityNumber][i]==1) {
				degree++;
			}
		}
		return degree;
	}
	public int[] getPath() {
		return this.m_path;
	}

	/*##############################################################################################################################*/
	/*--------------------------------------------------------THE-END--------------------------------------------------------------*/
	/*############################################################################################################################*/
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		String filename = "C:\\\\Users\\\\Glap\\\\Documents\\\\Java\\\\Eclipse\\\\EclipseWorkspace\\tsp-framework_tests\\instances\\eil101.tsp";
		Instance I = new Instance(filename,0);

		LKH_classic2 lkh = new LKH_classic2(I, "LKH");
		System.out.println(lkh.nbCandidates);
		//System.out.println(lkh.m_solution.isFeasible());
		lkh.solve();
		System.out.println("temps tout compris");
		System.out.println(System.currentTimeMillis()-startTime);
		//System.out.println(lkh.m_solution.isFeasible());
		
		
		
	}



	}



