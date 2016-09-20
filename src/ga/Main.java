package ga;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.script.*;

public class Main {
	static ScriptEngineManager manager;
	static ScriptEngine engine;
	static Random rng;
	static List<Chromosome> clist;
	
	static final int TARGET_VALUE = 42, EXPRESSION_LENGTH = 9, CROSSOVER_RATE = 7, MUTATION_RATE = 100;

	public static void main(String[] args) throws ScriptException {
		clist = new ArrayList<Chromosome>();
		manager = new ScriptEngineManager();
		engine = manager.getEngineByName("js");
		rng = new Random();
		
		//Generate a list of random valid chromosomes
		while (clist.size() < 100) {
			Chromosome c = new Chromosome();
			processChromosome(c);
			clist.add(c);
		}
		
		while (true) {
			List<Chromosome> temp = new ArrayList<Chromosome>();
			Collections.sort(clist);
			System.out.println(clist.get(0).score + "   " + clist.size());
			System.out.println("");
			while (clist.size() > 50) clist.remove(clist.size() - 1);
			
			while (clist.size() > 0) {
				Chromosome parent1 = clist.get(rng.nextInt(clist.size())), 
						parent2 = clist.get(rng.nextInt(clist.size()));
				clist.remove(parent1);
				clist.remove(parent2);
				Chromosome child1 = breedParents(parent1, parent2),
						child2 = breedParents(parent2, parent1);
				
				if(!processChromosome(child1) && !processChromosome(child2)) {
					temp.add(child1);
					temp.add(child2);
					temp.add(parent1);
					temp.add(parent2);
				} else return;
			}
			clist = new ArrayList<Chromosome>(temp);
		}	
	}
	
	public static boolean processChromosome(Chromosome child) {
		char[] expression = parseGenes(child.genes);
		try {
			child.score = assignScore(expression);
			if (child.score == 0f) {
				System.out.println("Solution found: " + new String(expression));
				return true;
			}
		} catch (ScriptException e) { child.score = Float.POSITIVE_INFINITY; }
		
		return false;
	}
	
	private static Chromosome breedParents(Chromosome chromosomeA, Chromosome chromosomeB) {
		Chromosome child = new Chromosome();
		child.genes = chromosomeA.genes.clone();
		
		int crossoverChance = rng.nextInt(10) + 1; 
		if (crossoverChance <= CROSSOVER_RATE) {
			int pos = rng.nextInt(EXPRESSION_LENGTH);
			for (int i = EXPRESSION_LENGTH - 1; i >= pos; i--) child.genes[i] = chromosomeB.genes[i];
		}
		
		for (int i = 0; i < child.genes.length; i++) { 
			int mutationChance = rng.nextInt(10000); 
			if (mutationChance <= MUTATION_RATE) chromosomeA.genes[i] += rng.nextInt(2)-1;
		}
		
		return child;
	}
	
	private static char[] parseGenes(int[] genes) {
		char[] express = new char[EXPRESSION_LENGTH];
		
		for (int i = 0; i < genes.length; i++) {
			if (genes[i] == 10) express[i] = '+';
			else if (genes[i] == 11) express[i] = '-';
			else if (genes[i] == 12) express[i] = '*';
			else express[i] = Integer.toString(genes[i]).toCharArray()[0];
		}
		return express;
	}
	
	private static float assignScore(char[] expression) throws ScriptException {
		return (float) Math.pow(TARGET_VALUE - Double.parseDouble(engine.eval(new String(expression)).toString()), 2);	
	}

	public static class Chromosome implements Comparable<Chromosome> {
		private int[] genes;
		private float score;
		
		public Chromosome() {
			genes = new int[Main.EXPRESSION_LENGTH];
			for (int i = 0; i < genes.length; i++) genes[i] = rng.nextInt(13);
		}
		public int compareTo(Chromosome compare) {
			return (int) Math.round(score - compare.score);
		}
	}
}
