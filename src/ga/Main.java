package ga;

import java.util.ArrayList;
import java.util.Random;

import javax.script.*;

/**
 * Demonstrates an example genetic algorithm that evolves solutions (mathematical expressions) to find the target value
 * @author Edward Higgins
 *
 */
public class Main {

	static ScriptEngineManager manager;
	static ScriptEngine engine;
	static Random rng;
	
	static ArrayList<Chromosome> clist;
	
	static final int TARGET_VALUE = 42;
	static final int EXPRESSION_LENGTH = 9;
	static final int CROSSOVER_RATE = 7; // 7/10 = 70%
	static final int MUTATION_RATE = 1; // 1/10000 = 0.01%

	public static void main(String[] args) throws ScriptException {
	
		clist = new ArrayList<Chromosome>();
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
		rng = new Random();
		
		//Generate a list of random valid chromosomes
		for (int i = 0; i < 100; i++) {
			Chromosome chromosome = new Chromosome();
			char[] expression = parseGenes(chromosome.genes();
			try {
				chromosome.score = assignScore(expression);
				if (chromosome.score >= 0) clist.add(chromosome);
			} catch (ArithmeticException e) {
				System.out.println("Solution found: " + new String(expression));
				return;
			} catch (ScriptException e) { continue; }
		}

		//Breed parents and evolve new offspring
		while (true) {
			Chromosome parent1 = getParent();
			Chromosome parent2 = getParent();

			for (int i = 0; i < 2; i++) {
				Chromosome newChromosome;
				
				if (i == 0) newChromosome = breedParents(parent1, parent2);
				else newChromosome = breedParents(parent2, parent1);
				
				char[] expression = parseGenes(newChromosome.genes);
				try {
					newChromosome.score = assignScore(expression);
					if(newChromosome.score < 0) continue;
				} catch (ArithmeticException e) {
					System.out.println("Solution found: " + new String(expression));
					return;
				} catch (ScriptException e) { continue; }
				clist.add(newChromosome);
			}
			clist.add(parent1);
			clist.add(parent2);
		}
	}
	
	/**
	 * Breed both parent chromosomes together to produce a new offspring chromosome
	 * <p>
	 * Applies crossover and mutation to chromosome A's genes to create offspring
	 * @param chromosomeA parent A
	 * @param chromosomeB parent B
	 * @return new chromosome containing either parent A's genes or new genes
	 */
	private static Chromosome breedParents(Chromosome chromosomeA, Chromosome chromosomeB) {
		int[] tempGenes = new int[EXPRESSION_LENGTH];
		
		//Crossover the parent's genes
		int crossoverChance = rng.nextInt(10) + 1; 
		if (crossoverChance <= CROSSOVER_RATE) {
			int pos = rng.nextInt(EXPRESSION_LENGTH);
			
			//Replace genes
			for (int i = 0; i <= pos; i++) tempGenes[i] = chromosomeA.genes[i];
			for (int i = tempGenes.length-1; i >= pos; i--) tempGenes[i] = chromosomeB.genes[i];
			chromosomeA.genes = tempGenes;
		}
		
		//Mutate the genes
		for (int i = 0; i < tempGenes.length-1; i++) { 
			int mutationChance = rng.nextInt(10000) + 1; 
			if (mutationChance == MUTATION_RATE) {
				tempGenes = chromosomeA.genes;
				tempGenes[i] += rng.nextInt(3)-1;
				chromosomeA.genes = tempGenes;
			}
		}
		
		return chromosomeA;
	}
	
	/**
	 * Select a chromosome to breed using roulette-wheel selection and remove from the population to avoid selecting twice
	 * @return selected chromosome
	 */
	private static Chromosome getParent() {
		//Sum fitness scores
		float sum = 0;
		for (Chromosone c : clist) sum += c.score;
		
		//Generate random number
		float select = rng.nextFloat() * sum;
		
		//Select a parent
		sum = 0;
		for (int i = 0; i < clist.size(); i++) {
			sum += clist.get(i).score;
			if (sum + 1 > select) {
				Chromosome tmp = clist.get(i);
				clist.remove(i);
				return tmp;
			}
		}
		 
		throw new RuntimeException("No chromosomes in list");
	}
	
	/**
	 * Takes a set of genes and turns them into a valid mathematical expression
	 * @param genes int array of genes from chromosome
	 * @return mathematical expression in char array form
	 */
	private static char[] parseGenes(int[] genes) {
		char[] express = new char[EXPRESSION_LENGTH];
		
		for (int i = 0; i < genes.legnth; i++) {
			if (genes[i] == 10) express[i] = '+';
			else if (genes[i] == 11) express[i] = '-';
			else if (genes[i] == 12) express[i] = '*';
			else express[i] = Integer.toString(genes[i]).toCharArray()[0];
		}
		return express;
	}
	
	/**
	 * Fitness function - assigns fitness scores to chromosomes
	 * <p>
	 * The fitness score is the inverse of the difference between the target and the value expressed by the chromosome
	 * @param expression mathematical expression expressed by a chromosome
	 * @return Fitness score as determined above
	 * @throws ScriptException raised by JS engine if script is invalid - should never happen
	 */
	private static float assignScore(char[] expression) throws ScriptException {
	
		//Execute the script with the JS engine
		String tmp = new String(expression);
		Object val = engine.eval(tmp);
		float fitnessScore = (float) (1/(TARGET_VALUE - Double.parseDouble(val.toString())));
		
		//If script produces target value
		if (Double.isInfinite(fitnessScore)) throw new ArithmeticException();
		return fitnessScore;
		
	}

	public class Chromosome {

		private int[] genes;
		private float score;
		
		public Chromosome() {
			genes = new int[Main.EXPRESSION_LENGTH];
			for (int i = 0; i < genes.length; i++) genes[i] = rng.nextInt(13);
		}
		
	}

}
