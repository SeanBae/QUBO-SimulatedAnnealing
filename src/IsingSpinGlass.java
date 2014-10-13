/*	Name: 		Sean Bae
 * 	Project: 	Classical Computer vs. D-WAVE One
 * 	Detail: 	This program implements the Simulated Annealing algorithm
 * 				to solve the Ising Spin Glass Ground State Problem faster
 * 				than the D-WAVE One, the first alleged quantum computer
 * 				that was designed to solve this particular problem
 */

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Random;

public class IsingSpinGlass {

	public static void main(String[] args) throws IOException {

		/*
		 * Total computing time goal: 2.5 seconds = 2500 milliseconds; n = 108;
		 * 1000 instances
		 * 
		 * H = - sum{i<j}{J_ij * z_i * z_j}
		 * 
		 * Input: 1. n particles 2. J_ij's are the coupling constants; element
		 * of {-1,1}; for each 1 <= i < j <= n
		 * 
		 * Goal: Find an assignment z_i (which is an element of {-1,1}) that
		 * minimizes H
		 * 
		 * Pseudo-code at high level:
		 * 
		 * Generate random state z_i's 
		 * temperature = some high constant
		 * for
		 * (some number of times): pick random i new state = old state except
		 * z_i's sign is changed if H_new <= H_old: accept new state if H_new >
		 * H_old: accept new state with probability
		 * exp(-(H_new-H_old)/temperature) decrease temperature by small amount
		 */

		// start the runtime counter
		final long startTime = System.currentTimeMillis();

		// read the D-WAVE One's quantum annealing data
		String fileName = "/Users/bgm9103/Desktop/dwave/Benchmarking--108q--15us--02-Aug-2012-13-55-20.txt";
		Path path = Paths.get(fileName);
		Scanner scanner = new Scanner(path);
		scanner.useDelimiter(System.getProperty("line.separator"));

		// print test case number
		System.out.println("Test case: " + fileName.substring(29));

		// set static parameters
		final int num_particles = 108;
		final int energy = Integer.parseInt(scanner.next().substring(123));

		// set dynamic parameters
		final int trials = 10;
		final int subtrials = 100;
		final int annealing_steps = 10000;
		final double rate = 0.99;
		double temp = 100;

		// initialize variables
		int success = 0;
		int H = 0;
		int tmp;
		int num_lines = 0;

		// count the number of coupling data
		while (scanner.hasNext()) {
			scanner.next();
			num_lines++;
		}
		scanner.close();

		// new scanner to start over
		scanner = new Scanner(path);
		scanner.useDelimiter(System.getProperty("line.separator"));

		// declare variables to construct data structure
		String data;
		String[] tokens;
		int z_i, z_j, J_ij;
		Couplings couplings[] = new Couplings[num_lines];
		
		// read to skip the first line
		scanner.next();

		// read the coupling data and put them in an array
		while (scanner.hasNext()) {
			data = scanner.next();
			tokens = data.split("\\s+");

			z_i = Integer.parseInt(tokens[0]);
			z_j = Integer.parseInt(tokens[1]);
			J_ij = Integer.parseInt(tokens[2]);

			// fill up array backwards
			couplings[num_lines - 1] = new Couplings(z_i, z_j, J_ij);
			num_lines--;
		}
		scanner.close();
		
		Random rand = new Random();

		// computing begins
		for (int i = 0; i < trials; i++) {
			System.out.print("Trial " + (i + 1) + "...");

			// subtrials perform simulated annealing attempts on the same random
			// generated sets
			for (int j = 0; j < subtrials; j++) {
				// array to hold particles
				int[] particles = new int[num_particles];

				// Generate random state z_i's
				for (int n = 0; n < num_particles; n++) {
					if (rand.nextDouble() >= 0.5)
						particles[n] = -1;
					else
						particles[n] = 1;
				}
				
				// generate new list of particles and feed it into the compute function
				tmp = compute(couplings, particles, energy, annealing_steps,
						temp, rate);

				// take the best solution
				if (H > tmp)
					H = tmp;
				//System.out.println(H);
//				if (energy == H)
//					break;
			}
			if (energy == H)
				success++;
			System.out.print("energy: " + energy + "...");
			System.out.print("success: ");
			System.out.print(energy == H);
			System.out.println("...running time: " + (double)Math.round((System.currentTimeMillis()-startTime)/((i+1)*1000.0)*10000)/10000);
			// re-initialize for the next trial
			H = 0;
		}

		// end the runtime counter
		final long endTime = System.currentTimeMillis();
		final long totalTime = endTime - startTime;

		System.out.println();
		System.out.println("Test case: " + fileName.substring(29));
		System.out.println("Global Optimum: " + energy);
		System.out.println("Total number of trials: " + trials);
		System.out.println("Total number of success(es): " + success);
		System.out.println("Average running time: " + totalTime
				/ (1000.0 * trials));
		System.out.println("Success probability: " + success * 100.0 / trials
				+ "%");

	}

	public static int compute(Couplings[] couplings, int[] particles,
			int energy, int annealing_steps, double temp, double rate)
			throws IOException {

		Random rand = new Random();

		// new state = old state except z_i is flipped
		for (int k = 0; k < annealing_steps; k++) {
			int H_old = H(couplings, particles);
			int i = rand.nextInt(particles.length);

			// optimization: only change particle with non-zero couplings
			particles[i] *= -1;

			// optimization: it's not necessary to calculate all out again
			int H_new = H(couplings, particles);

			// accept the new state with probability exp(-(H_new-H_old)/temp)
			if (H_new > H_old
					&& Math.exp((H_old - H_new) / temp) < Math.random())
				particles[i] *= -1;

			// lower the temperature non-linearly
			temp *= rate;

		}
		
		int H = H(couplings, particles);
		//System.out.println("Local Optimum: " + H);

		return H;
	}

	// Method to calculate total energy
	public static int H(Couplings[] coupling, int[] particles) {

		int H = 0;
		for (int i = 0; i < coupling.length; i++) {
			H -= coupling[i].J_ij * particles[coupling[i].z_i - 1]
					* particles[coupling[i].z_j - 1];
		}
		return H;
	}

}
