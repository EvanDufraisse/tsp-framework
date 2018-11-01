package tsp.heuristic;

import java.util.ArrayList;
import java.util.List;

import tsp.Instance;

// TODO: Auto-generated Javadoc
/**
 * The Class LKH_classic.
 */
public class LKH_classic extends AHeuristic{


/*#########################################################################################################################*/
/*-----------------------------------------------VARIABLES DINSTANCES--------------------------------------------------------------*/
/*#########################################################################################################################*/	
public int[][] m_1_Tree;
/*m_1_Tree est un tableau binaire de taille n*n avec m_1_Tree[i][j] == 1 si la liaison 
i----j apartient à l'arbre et 0 sinon */

 public List<Integer> m_topological_prec_spanning_tree;
/*Cette variable d'instance permet de connaitre l'ordre dans lequel
les villes ont été ajoutée au Spanning-Tree, arbre qui précède la création du 1-Tree, ceci est utile pour calculer
m_alpha_nearness et m_dad ultérieurement */


public int[] m_dad;
/*m_dad est une liste qui indique par m_dad[City1] la ville parent de City1 dans le Spanning-Tree*/


public int m_special_node;
/*Il s'agit du noeud "spécial" qui permet de former le 1-Tree à partir du Spanning-Tree*/


public long[][] m_alpha_nearness; 
/*Il s'agit d'une distance non euclidienne qui permet de savoir quels sont les 
noeuds les plus probablement reliés ensemble*/


 int[][] alpha_candidates;
/*Il s'agit d'un tableau extrait de m_alpha_nearness de taille n*K ou K désigne 
le nombre de meilleurs candidats que l'on souhaite garder par noeud, on prend généralement K=5 */


public int[]  m_path; 
/* Il s'agit du chemin parcouru avec la même norme que m_cities de la classe solution, on 
recopie directement m_path dans m_cities */


 public int[] m_successor;
/*m_successor[City1] == City2 veut dire que dans le chemin actuel City1 précède City2*/


public int[] m_predecessor;
/*m_predecessor[City1] == City2 veut dire que dans le chemin actuel City1 succède à City2*/


public int[] m_path_indexed_by_cities;
/*m_path_indexed_by_cities[City1] == i veut dire que City1 est la 
i+1ème ville visitée pour le chemin actuel*/
	
public int[][] m_euclidean_candidates;

public int[][] m_K_permutation;

/*#########################################################################################################################*/
/*-----------------------------------------------CONSTRUCTEUR & INITIALISATION--------------------------------------------------------------*/
/*#########################################################################################################################*/

/**
 * Ce constructeur prend en entrée l'instance et construit toutes les variables d'instances nécessaires au problème*.
 *
 * @param instance the instance
 * @param name the name
 * @throws Exception the exception
 */


public LKH_classic(Instance instance, String name) throws Exception {
	super(instance, name);
	this.m_1_Tree= new int[this.m_instance.getNbCities()][this.m_instance.getNbCities()];
	this.m_dad = new int[this.m_instance.getNbCities()];
	this.m_special_node=-2;
	this.PrimAlgorithm();
	this.beta_nearness();
	//System.out.println(this.m_special_node);
	//System.out.println("degreemoy"+ this.DegreeMoyen());
	this.OneTreeOptimisation();
	long[][] a = this.alpha_nearness();
	this.FirstTourConstruction(this.candidates(a));
	this.predecessor();
	this.successor();
	this.pathIndexedByCities();
	this.m_K_permutation=Kpermutation();
	this.EuclideanCandidatesGenerator();
	
}
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/**
 * PrimAlgorithm est basé sur l'algorithme de PRIM pour former un Minimum Spanning-Tree et au final un 1-Tree.
 * Le 1-Tree est stocké sous forme d'une matrice carré binaire this.m_1_Tree où this.m_1_Tree[i][j]==1 veut dire
 * que les villes i et j sont reliées.
 * STEP#1 On construit un minimum spanning-tree avec toutes les villes sauf une au choix.
 * STEP#2 A partir de cette dernière ville non reliée , on construit les deux segmentsqui forment
 * les deux distances minimum avec le spanning Tree.
 * La valeur objective this.ObjectiveValue() de ce graphe permet d'obtenir une borne inférieure du problème du TSP.
 * @throws Exception the exception
 */

public void PrimAlgorithm() throws Exception {
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
				////System.out.println(Arrays.deepToString(this.m_1_Tree));
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Beta nearness.
 *Le calcul de la "Beta nearness" est intermediaire au calcul de "l'alpha nearness".
 *La matrice beta retournée donne la longueur beta[i][j] de la liaison minimal à casser
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
			////System.out.println("betakdadl"+beta[k][this.m_dad[l]]+ "distance l dadl" + this.m_instance.getDistances(l, this.m_dad[l]));
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
/*----------------------------------------------------------------------------------------------------------------------*/
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
public void OneTreeOptimisation() throws Exception {
	int n = this.m_1_Tree.length;
	long[] v1 = new long[n];
	long[] v2 = new long[n];
	long[] pi = new long[n];
	int c = 0;
	double t =100; // peut etre changer t au profit d'un compromis avec les distances du problème
	for(int i=0; i<n; i++) { //peut etre faire une fonction de copie des vecteurs
		v1[i]=this.degree(i)-2;
		v2[i]=v1[i];
		pi[i]=(long)(v1[i]*t);
	}
	int period = n/2;
	long objectiveValue1= this.ObjectiveValue();
	long objectiveValue2=objectiveValue1;
	while(period!=0 && t>0 && c!=n ) {
		////System.out.println(period);
	for(int p=0; p<period; p++) {
		if(objectiveValue1==objectiveValue2) {
		t=t*2;
		//System.out.println("ça commence");
		}
		else {
			objectiveValue1=-1;
			t=t/2;
			period = (int) period/2;
			
		}
		for(int i=0; i<n; i++) {
			//double a=(pi[i]+(0.7*v1[i]+0.3*v2[i])*t);
			double a=(pi[i]+v1[i]*t);
			pi[i]= (long) a;
			v2[i]=v1[i];
			
		}
			this.PrimAlgorithm2(pi);
			c=0;
			for(int i=0; i<n; i++) {
				v1[i]=this.degree(i)-2;
				if(v1[i]==0) {
					c++;}
				}
			if(objectiveValue2<this.ObjectiveValue()) {
				period=period*4;
				t=t*2;
			}
			objectiveValue2=this.ObjectiveValue();
			////System.out.println(objectiveValue2);
				
			
	}}
	//System.out.println("period "+period);
	//System.out.println("c "+c);
	//System.out.println("t "+t);
	return;
}

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Alpha nearness.
 *L'alpha nearness se calcule à partir de la beta nearness et correspond au coût d'ajout de la liaison i--j 
 *dans le 1-Tree. Plus cette valeur est grande moins l'on considère la laiison comme probable par la suite.
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


/**
 * First tour construction.
 *Pour chaque ville on possède par ordre décroissant de probabilité (selon le critère d'alpha nearness) les villes auquelles
 *elle pourrait être reliée. On construit un premier tour en prenant la ville la plus probable encore disponible.
 *Le résultat est meilleur qu'un résultat aléatoire et qu'un "plus proche voisin" et peu coûteux puisque le calcul
 *des candidats doit être fait pour l'amélioration ultérieure du tour.
 * @param candidates the candidates
 * @return the int[]
 * @throws Exception the exception
 */
public int[] FirstTourConstruction(int[][] candidates) throws Exception {
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

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

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


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

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


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


/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


/*#########################################################################################################################*/
/*-----------------------------------------------AMELIORATION DU TOUR--------------------------------------------------------------*/
/*#########################################################################################################################*/

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


/* (non-Javadoc)
 * @see tsp.heuristic.AHeuristic#solve()
 */
public void solve() throws Exception {
	long startTime = System.currentTimeMillis();
	if(!this.m_solution.isFeasible()) {
	int[][] tab = this.alpha_candidates;
	int n = this.m_instance.getNbCities();
	
	
	boolean FourOpt= false;
	boolean FiveOpt= false;
	long length=this.ObjectiveValuePath()+1;
	while(FourOpt==false||FiveOpt==false||length!=this.ObjectiveValuePath()) {
		if(length==this.ObjectiveValuePath()) {
			if(FourOpt==false) {
			FourOpt=true;
			////System.out.println("4-Opt-started");
			}
			else {
				FiveOpt=true;
				////System.out.println("5-Opt-started");
			}
		}
		length=this.ObjectiveValuePath();
	for(int t1=0;t1<n;t1++) {
		for(int x2=1; x2<3;x2++) {
			int t2 = (x2==1)? this.m_successor[t1] : this.m_predecessor[t1];
			this.LkhMove(t1,t2,FourOpt,FiveOpt,tab);
		}
	}
	}
	for(int w = 0; w<this.m_instance.getNbCities()-11; w++) {
		if(System.currentTimeMillis() - startTime>57000) {
			for(int i=0; i<this.m_path.length;i++) {
				this.m_solution.setCityPosition(this.m_path[i], i);
					}
			System.out.println(this.ObjectiveValuePath());
			return;
		}
		this.permutationOpti(this.m_path[w]);
		////System.out.println(this.ObjectiveValuePath());
	}
	for(int w = 0; w<this.m_instance.getNbCities(); w++) {
		if(System.currentTimeMillis() - startTime>57000) {
			for(int i=0; i<this.m_path.length;i++) {
				this.m_solution.setCityPosition(this.m_path[i], i);
					}
			System.out.println(this.ObjectiveValuePath());
			return ;
		}
		this.MakeEuclidean2Opt(w);
		//System.out.println(this.ObjectiveValuePath());
	}
	}
	for(int i=0; i<this.m_path.length;i++) {
		this.m_solution.setCityPosition(this.m_path[i], i);
			}
	System.out.println(this.ObjectiveValuePath());
	return ;
}

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
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
 */
public void LkhMove(int t1 , int t2, boolean fourOpt, boolean fiveOpt, int[][] alpha_candidates) throws Exception {
List<Integer> liste = new ArrayList<Integer>();
liste.add(t1);
liste.add(t2);
long ObjectiveValue = this.ObjectiveValuePath();
long G0=this.m_instance.getDistances(t1, t2);
for(int k1 = 0; k1< alpha_candidates[t2].length; k1++) {
	int t3 = alpha_candidates[t2][k1];
	long G1=G0-this.m_instance.getDistances(t2, t3);
	if(t3==this.m_predecessor[t2]||t3==this.m_successor[t2]||G1<=0) {
		continue;
	}
	liste.add(t3);
	for(int x4=1; x4<3;x4++) {
		int t4 = (x4==1)? this.m_successor[t3] : this.m_predecessor[t3];
		long G2 = G1+this.m_instance.getDistances(t3, t4);
		long Gain = G2-this.m_instance.getDistances(t1, t4);
		if(x4==1 && Gain>0 && t2!=t4 && this.m_successor[t1]!=t2) {
			this.swap(t2, t4);
			////System.out.println(Arrays.toString(this.m_path));
			long newObj=this.ObjectiveValuePath();
			if(ObjectiveValue>newObj) {
				ObjectiveValue=newObj;
				//System.out.println(this.ObjectiveValuePath());
				return;
			}
			else {
				this.swap(t4, t2);
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
				boolean valid = this.Make3Opt(t1,t2,t3,t4,t5,t6);
				if(valid==true) {
					return;
				}if(fourOpt==true) {
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
						boolean valid2 = this.Make4Opt(t1,t2,t3,t4,t5,t6,t7,t8);
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
									boolean valid3 = this.Make5Opt(t1,t2,t3,t4,t5,t6,t7,t8,t9,t10);
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

}
return;}

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


/**
 * Swap.
 *
 * @param i the i
 * @param j the j
 */
public void swap(int i, int j) {
int first = (this.m_path_indexed_by_cities[i]<this.m_path_indexed_by_cities[j])? i : j;
int last = (this.m_path_indexed_by_cities[i]<this.m_path_indexed_by_cities[j])? j : i;
int index1 = this.m_path_indexed_by_cities[first];
int index2 = this.m_path_indexed_by_cities[last];
int w= index2 - index1+1;
int[] temp = new int[w];
for(int g =0; g<w; g++) {
	temp[g]=this.m_path[index2-g];
}
if(first!=0) {
for(int k = 0; k<w;k++) {
	this.m_path[k+index1]=temp[k];
}}
else{
	int n=this.m_path.length;
	int[] temp2 = new int[n];
	temp2[0]=0;
	for(int h1= w; h1<n-1;h1++) {
		temp2[h1-w+1]=this.m_path[h1];
	}
	temp2[n-w]=temp[0];
	temp2[n-1]=temp[w-1];
	for(int h2=1; h2<w-1;h2++) {
		temp2[n-w+h2]=temp[w-h2-1];
		
	}
	this.m_path=temp2;
	
}
this.successor();
this.predecessor();
this.pathIndexedByCities();
}


/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Make 5 opt.
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
private boolean Make5Opt(int t1, int t2, int t3, int t4, int t5, int t6, int t7, int t8, int t9, int t10) throws Exception {
	long Obj = this.ObjectiveValuePath();
	long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6)+this.m_instance.getDistances(t7,t8)+this.m_instance.getDistances(t9, 10);
	long G1= G0-this.m_instance.getDistances(t1, t9)-this.m_instance.getDistances(t2, t3)-this.m_instance.getDistances(t4,t5)-this.m_instance.getDistances(t6, t7)-this.m_instance.getDistances(t8, t9);
	if(G1>0) {
		////System.out.println("3-opt");
		this.swap(t2, t10);
		this.swap(t2, t4);
		this.swap(t4, t6);
		this.swap(t6, t8);
	}
	if(Obj<this.ObjectiveValuePath()) {
		this.swap(t6, t8);
		this.swap(t4, t6);
		this.swap(t2, t4);
		this.swap(t2, t10);
		return false;
		////System.out.println("undo 3-opt");
	}
	else {
		//System.out.println("success 5opt");
		return true;
	}
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
public boolean Make3Opt(int t1, int t2, int t3, int t4, int t5, int t6) throws Exception {
	long Obj = this.ObjectiveValuePath();
	long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6);
	long G1= G0-this.m_instance.getDistances(t1, t6)+this.m_instance.getDistances(t2, t3)+this.m_instance.getDistances(t4,t5);
	if(G1>0) {
		////System.out.println("3-opt");
		this.swap(t2, t6);
		this.swap(t2, t4);
	}
	if(Obj<this.ObjectiveValuePath()) {
		this.swap(t2, t4);
		this.swap(t2, t6);
		return false;
		////System.out.println("undo 3-opt");
	}
	else {
		//System.out.println("success 3opt");
		return true;
	}
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
public boolean Make4Opt(int t1,int t2,int t3,int t4,int t5,int t6,int t7,int t8) throws Exception {
	long Obj = this.ObjectiveValuePath();
	long G0 = this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)+this.m_instance.getDistances(t5,t6)+this.m_instance.getDistances(t7,t8);
	long G1= G0-this.m_instance.getDistances(t1, t8)-this.m_instance.getDistances(t2, t3)-this.m_instance.getDistances(t4,t5)-this.m_instance.getDistances(t6, t7);
	if(G1>0) {
		////System.out.println("3-opt");
		this.swap(t2, t8);
		this.swap(t2, t4);
		this.swap(t4, t6);
	}
	if(Obj<this.ObjectiveValuePath()) {
		this.swap(t4, t6);
		this.swap(t2, t4);
		this.swap(t2, t8);
		return false;
		////System.out.println("undo 3-opt");
	}
	else {
		////System.out.println("success 4opt");
		return true;
	}
}

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Superieur.
 *
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Candidates.
 *
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
	int nbCandidates =8;
	int[][] alpha_candidates = new int[n][nbCandidates];
	for(int g = 0 ; g<n; g++) {
		for(int j =0; j<nbCandidates;j++) {
			alpha_candidates[g][j]=candidates[g][j];
		}
	}
	this.alpha_candidates=alpha_candidates;
	return candidates;
	
	
}

/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Objective value.
 *
 * @return the long
 * @throws Exception the exception
 */
public long ObjectiveValue() throws Exception {
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
 *
 * @return the long
 * @throws Exception the exception
 */
public long ObjectiveValuePath() throws Exception {
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
 *
 * @return the double
 */
public double DegreeMoyen() {
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
 */
public void NombreDegreeDeux() {
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
 * Prim algorithm 2.
 *
 * @param pi the pi
 * @throws Exception the exception
 */
public void PrimAlgorithm2(long[] pi) throws Exception {
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/


/**
 * Maximum.
 *
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
 *
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
 *
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
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/
/*----------------------------------------------------------------------------------------------------------------------*/

/**
 * Permutation opti.
 *
 * @param t1 the t 1
 * @throws Exception the exception
 */
public void permutationOpti(int t1) throws Exception {
	this.successor();
	this.pathIndexedByCities();
	int Koef = 12;
	int[] liste = new int[Koef];
	liste[0]=t1;
	int min =-1;
	for(int i = 1; i<Koef;i++) {
		liste[i]=this.m_successor[liste[i-1]];
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
 */
private static int[][] Kpermutation() {
	int k =10;//k<10
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

/**
 * Euclidean candidates generator.
 *
 * @throws Exception the exception
 */
public void EuclideanCandidatesGenerator() throws Exception {
	int n = this.m_instance.getNbCities();
	int k =20;
	int[][] euclideanCandidates = new int[n][k];
	for(int i = 0; i<n; i++) {
		for(int w= 0; w<k; w++) {
			euclideanCandidates[i][w] = 0;
		}
		for(int j=0; j<n;j++) {
			int g=0;
			int c=0;
		while(c==0) {
			if(this.m_instance.getDistances(euclideanCandidates[i][g],i)>this.m_instance.getDistances(i, j)) {
				c=1;
				euclideanCandidates[i][g]=j;
			}
			else {
				if(g<k-1) {
					g++;
				}
				else {
					c=1;
				}
				
			}
		}
	}
		}
	this.m_euclidean_candidates=euclideanCandidates;
}

/**
 * Make euclidean 2 opt.
 *
 * @param t1 the t 1
 * @throws Exception the exception
 */
public void MakeEuclidean2Opt(int t1) throws Exception {
	int t2 = this.m_successor[t1];
	long obj = this.ObjectiveValuePath();
	for(int i=0; i<this.m_euclidean_candidates[t1].length;i++) {
		int t3 = this.m_euclidean_candidates[t1][i];
		if(t3!=t1 && t3!=t2) {
			int t4 = this.m_successor[t3];
			long d=0;
			if(t4!=t1 && t4!=t2) {
				d=this.m_instance.getDistances(t1, t2)+this.m_instance.getDistances(t3, t4)-this.m_instance.getDistances(t1, t3)-this.m_instance.getDistances(t2, t4);
				if(d>0) {
					//System.out.println("Euclidean opti");
					//System.out.println(d);
					this.swap(t3, t2);
					if(this.ObjectiveValuePath()>obj) {
						this.swap(t3, t2);
					}
				}
				
				
		}
	}
	}
}

/**
 * The main method.
 *
 * @param args the arguments
 * @throws Exception the exception
 */
public static void main(String[] args) throws Exception {
	String filename = "D:\\School\\A2\\EclipseWorkspace\\tsp-framework_tests\\instances\\eil51.tsp";
	Instance I = new Instance(filename,0);
	LKH_classic lkh = new LKH_classic(I, "LKH");
	System.out.println(lkh.m_solution.isFeasible());
	lkh.solve();
	System.out.println(lkh.m_solution.isFeasible());
	
}



}

