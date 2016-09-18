package ga;

public class Chromosome {

	private int[] genes;
	private float fitnessScore;
	
	/**
	 * Create a new Chromosome object
	 */
	public Chromosome() {
		genes = new int[Main.EXPRESSION_LENGTH];
		
		for(int i = 0; i < genes.length; i++) {
			genes[i] = Main.rng.nextInt(13);
		}
	}
	
	public int[] getGenes() {
		return this.genes;
	}
	
	public void setGenes(int[] genes) {
		this.genes = genes;
	}

	public float getScore() {
		return fitnessScore;
	}

	public void setScore(float score) {
		this.fitnessScore = score;
	}
	
}
