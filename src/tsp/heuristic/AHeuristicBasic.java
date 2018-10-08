package tsp.heuristic;

import tsp.Instance;
import tsp.Solution;

public class AHeuristicBasic extends AHeuristic {

	public AHeuristicBasic(Instance instance, String name) throws Exception {
		super(instance, name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void solve() throws Exception {
		// TODO Auto-generated method stub
		Solution solution = new Solution(this.m_instance);
		
		for(int i = 1; i < this.m_instance.getNbCities(); i++) {
			solution.setCityPosition(i, i);
		}
		solution.setCityPosition(0, this.m_instance.getNbCities());
		this.m_solution = solution;
	}

}
