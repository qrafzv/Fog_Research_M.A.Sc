//  NSGAII_main.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.metaheuristics.nsgaII;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.smpso.SMPSO;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.SelectionFactory;
import jmetal.problems.ProblemFactory;
import jmetal.problems.carleton.FPP.FogPlanningProblem;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * Class to configure and execute the NSGA-II algorithm.
 * 
 */

public class PSONSGAII_main {
	public static Logger logger_; // Logger object
	public static FileHandler fileHandler_; // FileHandler object
	public static SolutionSet midSet;

	/**
	 * @param args
	 *            Command line arguments.
	 * @throws JMException
	 * @throws IOException
	 * @throws SecurityException
	 *             Usage: three options - jmetal.metaheuristics.nsgaII.NSGAII_main -
	 *             jmetal.metaheuristics.nsgaII.NSGAII_main problemName -
	 *             jmetal.metaheuristics.nsgaII.NSGAII_main problemName
	 *             paretoFrontFile
	 */
	public static void main(String[] args) throws JMException, SecurityException, IOException, ClassNotFoundException {
		Problem problem; // The problem to solve
		Algorithm algorithm; // The algorithm to use
		Operator crossover; // Crossover operator
		Operator mutation; // Mutation operator
		Operator selection; // Selection operator

		HashMap parameters; // Operator parameters

		QualityIndicator indicators; // Object to get quality indicators

		// Logger object and file to store log messages
		logger_ = Configuration.logger_;
		fileHandler_ = new FileHandler("NSGAII_main.log");
		logger_.addHandler(fileHandler_);

		indicators = null;
		if (args.length == 1) {
			Object[] params = { "Real" };
			problem = (new ProblemFactory()).getProblem(args[0], params);
		} // if
		else if (args.length == 2) {
			Object[] params = { "Real" };
			problem = (new ProblemFactory()).getProblem(args[0], params);
			indicators = new QualityIndicator(problem, args[1]);
		} // if
		else { // Default problem
			// problem = new Kursawe("Real", 3);
			// problem = new Kursawe("BinaryReal", 3);
			// problem = new Water("Real");
			// problem = new ZDT3("ArrayReal", 30);
			// problem = new Ten2FiveProblem("Int");
			problem = new FogPlanningProblem("Int");
			// problem = new ConstrEx("Real");
			// problem = new DTLZ1("Real");
			// problem = new OKA2("Real") ;
		} // else

		// First Phase
		algorithm = new SMPSO(problem);

		// Algorithm parameters
		algorithm.setInputParameter("swarmSize", 1500);
		algorithm.setInputParameter("archiveSize", 1500);
		algorithm.setInputParameter("maxIterations", 7500);

		parameters = new HashMap();
		parameters.put("probability", 1.0 / problem.getNumberOfVariables());
		parameters.put("distributionIndex", 20.0);
		mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

		algorithm.addOperator("mutation", mutation);

		// Execute the Algorithm
		long initTime = System.currentTimeMillis();
		SolutionSet population = algorithm.execute();
		long estimatedTime = System.currentTimeMillis() - initTime;

		// Result messages
		logger_.info("First Phase Total execution time: " + estimatedTime + "ms");
		logger_.info("Objectives values have been writen to file FUN-alpha");
		population.printFeasibleFUN("FUN-alpha");
		logger_.info("Variables values have been writen to file VAR-alpha");
		population.printFeasibleVAR("VAR-alpha");

		// Second Phase
		midSet = population.feasibleEnforcement();

		algorithm = new NSGAII(problem);
		// algorithm = new ssNSGAII(problem);

		// Algorithm parameters
		algorithm.setInputParameter("populationSize", 1000);
		algorithm.setInputParameter("maxEvaluations", 2500);

		// Mutation and Crossover for Real codification
		parameters = new HashMap();
		parameters.put("probability", 0.9);
		parameters.put("distributionIndex", 20.0);
		crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

		parameters = new HashMap();
		parameters.put("probability", 1.0 / problem.getNumberOfVariables());
		parameters.put("distributionIndex", 20.0);
		mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

		// Selection Operator
		parameters = null;
		selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);

		// Add the operators to the algorithm
		algorithm.addOperator("crossover", crossover);
		algorithm.addOperator("mutation", mutation);
		algorithm.addOperator("selection", selection);

		// Add the indicator object to the algorithm
		algorithm.setInputParameter("indicators", indicators);

		// Execute the Algorithm

		initTime = System.currentTimeMillis();
		population = (algorithm).execute(midSet);
		estimatedTime = System.currentTimeMillis() - initTime;

		// Result messages
		logger_.info("Total execution time: " + estimatedTime + "ms");
		logger_.info("Variables values have been writen to file VAR-beta");
		population.printFeasibleVAR("VAR-beta");
		logger_.info("Objectives values have been writen to file FUN-beta");
		population.printFeasibleFUN("FUN-beta");

		// ten2fiveverification
		// new FPP_varification(problem);
		if (indicators != null) {
			logger_.info("Quality indicators");
			logger_.info("Hypervolume: " + indicators.getHypervolume(population));
			logger_.info("GD         : " + indicators.getGD(population));
			logger_.info("IGD        : " + indicators.getIGD(population));
			logger_.info("Spread     : " + indicators.getSpread(population));
			logger_.info("Epsilon    : " + indicators.getEpsilon(population));

			int evaluations = ((Integer) algorithm.getOutputParameter("evaluations")).intValue();
			logger_.info("Speed      : " + evaluations + " evaluations");
		} // if
	} // main
} // NSGAII_main
