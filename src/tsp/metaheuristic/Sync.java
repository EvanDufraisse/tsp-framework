package tsp.metaheuristic;

public class Sync {

	private int size;
	private Individu[] stock;
	private boolean disponible = false;
	
	
	public Sync(int size) {
		this.size= size;
		this.stock = new Individu[this.size];
	}

	public synchronized Individu[] get() throws InterruptedException {
		
		while (disponible == false) {
			wait();
			}
		
			disponible = false;
			notifyAll();
			return this.stock;
		}
	
	public synchronized void put(Individu[] val) throws InterruptedException {
		
		while (disponible == true) {
			wait();
			}
		
			this.stock = val;
			disponible = true;
			notifyAll();
			
		}
		
	public int getSize() {
		return this.size;
	}
}
