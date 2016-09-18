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

	/**
	 * Program entry point
	 * @param args not used
	 * @throws ScriptException if genes express an invalid script - should never happen, as script is validated by {@link #validateExpression(char[])}
	 */
	public static void main(String[] args) throws ScriptException {
	
		clist = new ArrayList<Chromosome>(100);
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
		rng = new Random();
		
		//Generate a list of random valid chromosomes
		while(true) {
			Chromosome chromosome = new Chromosome();
			char[] expression = parseGenes(chromosome.getGenes());
			if(!validateExpression(expression)) {
				continue;
			}
			
			try {
				chromosome.setScore(assignScore(expression));
			}
			catch(ArithmeticException e) {
				System.out.println("Solution found: " + new String(expression));
				return;
			}
			
			if(chromosome.getScore() < 0) { 
				continue; 
			}
			if(clist.size() == 100) { 
				break;
			}
			else { 
				clist.add(chromosome); 
			}
		}

		//Breed parents and evolve new offspring
		while(true) {
			Chromosome parent1 = getParent();
			Chromosome parent2 = getParent();

			for(int i = 0; i < 2; i++) {
				Chromosome newChromosome;
				
				if(i == 0) {
					newChromosome = breedParents(parent1, parent2);
				}
				else {
					newChromosome = breedParents(parent2, parent1);
				}
				
				char[] expression = parseGenes(newChromosome.getGenes());
				if(!validateExpression(expression)) {
					continue;
				}	
				try {
					newChromosome.setScore(assignScore(expression));
					if(newChromosome.getScore() < 0) {
						continue;
					}
				}
				catch(ArithmeticException e) {
					System.out.println("Solution found: " + new String(expression));
					return;
				}
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
		if(crossoverChance <= CROSSOVER_RATE) {
			int pos = rng.nextInt(EXPRESSION_LENGTH);
			
			//Replace genes
			for(int i = 0; i <= pos; i++) {
				tempGenes[i] = chromosomeA.getGenes()[i];
			}
			for(int i = tempGenes.length-1; i >= pos; i--) { 
				tempGenes[i] = chromosomeB.getGenes()[i];
			}
			chromosomeA.setGenes(tempGenes);
		}
		
		//Mutate the genes
		for(int i = 0; i < tempGenes.length-1; i++) { 
			int mutationChance = rng.nextInt(10000) + 1; 
			if(mutationChance == MUTATION_RATE) {
				tempGenes = chromosomeA.getGenes();
				tempGenes[i] += rng.nextInt(3)-1;
				chromosomeA.setGenes(tempGenes);
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
		for(int i = 0; i < clist.size(); i++) { 
			sum += clist.get(i).getScore();
		}
		
		//Generate random number
		float select = rng.nextFloat() * sum;
		
		//Select a parent
		sum = 0;
		for(int i = 0; i < clist.size(); i++) {
			sum += clist.get(i).getScore();
			if(sum + 1 > select) {
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
		
		for(int i = 0; i < genes.length; i++) {
			switch(genes[i]) {
			case 10:
				express[i] = '+';
				break;
			case 11:
				express[i] = '-';
				break;
			case 12:
				express[i] = '*';
				break;
			default:
				express[i] = Integer.toString(genes[i]).toCharArray()[0];
			}
		}
		return express;
	}
	
	/**
	 * Ensure the script expressed by the genes is valid
	 * <p>
	 * Script must follow the format Integer-operator-integer-etc.
	 * @param expression the script expressed by the genes
	 * @return true if valid script, false if not
	 */
	private static boolean validateExpression(char[] expression) {
		boolean lastInt = false;
		for(int i = 0; i < expression.length; i++) {
			if(expression[i] == '+' || expression[i] == '-' || expression[i] == '*') {
				if(lastInt) { 
					lastInt = false; 
				}
				else { 
					return false; 
				}
			}
			else {
				if(!lastInt) { 
					lastInt = true; 
				}
				else { 
					return false; 
				}
			}
		}
		return true;
	}
	
	/**
	 * Fitness function - assigns fitness scores to chromosomes
	 * <p>
	 * The fitness score is the inverse of the difference between the target and the value expressed by the chromosome
	 * @param expression mathematical expression expressed by a chromosome
	 * @return Fitness score as determined above
	 * @throws ScriptException raised by JS engine if script is invalid - should never happen, script validation handled by {@link #validateExpression(char[])}
	 */
	private static float assignScore(char[] expression) throws ScriptException {
		//Execute the script with the JS engine
		String tmp = new String(expression);
		Object val = engine.eval(tmp);
		float fitnessScore = (float) (1/(TARGET_VALUE - Double.parseDouble(val.toString())));
		
		//If script produces target value
		if(Double.isInfinite(fitnessScore)) { 
			throw new ArithmeticException(); 
		}
		
		return fitnessScore;
	}

}
