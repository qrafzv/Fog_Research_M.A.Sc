//  SMPSOStudy.java
//
//  Authors:
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

package jmetal.experiments.studies;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.core.Algorithm;
import jmetal.experiments.Experiment;
import jmetal.experiments.Settings;
import jmetal.experiments.settings.AbYSS_Settings;
import jmetal.experiments.util.Friedman;
import jmetal.util.JMException;

/**
 * Class implementing an example of experiment using NSGA-II as base algorithm.
 * The experiment consisting in studying the effect of the crossover probability
 * in NSGA-II.
 */
public class AbYSSStudy extends Experiment {
	/**
	 * Configures the algorithms in each independent run
	 * 
	 * @param problemName
	 *            The problem to solve
	 * @param problemIndex
	 * @param algorithm
	 *            Array containing the algorithms to run
	 * @throws ClassNotFoundException
	 */
	public synchronized void algorithmSettings(String problemName, int problemIndex, Algorithm[] algorithm)
			throws ClassNotFoundException {
		try {
			int numberOfAlgorithms = algorithmNameList_.length;

			HashMap[] parameters = new HashMap[numberOfAlgorithms];

			for (int i = 0; i < numberOfAlgorithms; i++) {
				parameters[i] = new HashMap();
			} // for

			if (!(paretoFrontFile_[problemIndex] == null) && !paretoFrontFile_[problemIndex].equals("")) {
				for (int i = 0; i < numberOfAlgorithms; i++)
					parameters[i].put("paretoFrontFile_", paretoFrontFile_[problemIndex]);
			} // if

			/*
			 * parameters[0].put("crossoverProbability_", 1.0);
			 * parameters[1].put("crossoverProbability_", 0.9);
			 * parameters[2].put("crossoverProbability_", 0.8);
			 * parameters[3].put("crossoverProbability_", 0.7);
			 */
			/*
			 * for(int i =0;i<3;i++){ for(int j =0;j<3;j++){
			 * parameters[i+j].put("maxEvaluations_", 2500+2500*j); Parameters[i+j].put()
			 * parameters[1].put("maxEvaluations_", 25000);
			 * parameters[2].put("maxEvaluations_", 50000); } }
			 */
			parameters[0].put("maxEvaluations_", 2500);
			parameters[1].put("maxEvaluations_", 25000);
			parameters[2].put("maxEvaluations_", 250000);

			/*
			 * parameters[0].put("archiveSize_", 500); parameters[1].put("archiveSize_",
			 * 1000); parameters[2].put("archiveSize_", 1500);
			 */
			/*
			 * parameters[0].put("swarmSize_", 500); parameters[1].put("swarmSize_", 1000);
			 * parameters[2].put("swarmSize_", 1500);
			 */
			// parameters[3].put("maxEvaluations_", 75000);

			/*
			 * if ((!paretoFrontFile_[problemIndex].equals("")) ||
			 * (paretoFrontFile_[problemIndex] == null)) { for (int i = 0; i <
			 * numberOfAlgorithms; i++) parameters[i].put("paretoFrontFile_",
			 * paretoFrontFile_[problemIndex]); }
			 */ // if

			for (int i = 0; i < numberOfAlgorithms; i++)
				algorithm[i] = new AbYSS_Settings(problemName).configure(parameters[i]);

		} catch (IllegalArgumentException ex) {
			Logger.getLogger(AbYSSStudy.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(AbYSSStudy.class.getName()).log(Level.SEVERE, null, ex);
		} catch (JMException ex) {
			Logger.getLogger(AbYSSStudy.class.getName()).log(Level.SEVERE, null, ex);
		}
	} // algorithmSettings

	public static void main(String[] args) throws JMException, IOException {
		AbYSSStudy exp = new AbYSSStudy(); // exp = experiment

		exp.experimentName_ = "AbYSSStudyEvalution";
		exp.algorithmNameList_ = new String[] { "AbYSSS_2500", "AbYSSS_25000", "AbYSSS_250000" };
		exp.problemList_ = new String[] { "FogPlanningProblem3010" };
		/*
		 * exp.problemList_ = new String[] { "ZDT1", "ZDT2", "ZDT3", "ZDT4", "DTLZ1",
		 * "WFG2"} ;
		 */
		exp.paretoFrontFile_ = new String[1]; // {
		// "ZDT1.pf", "ZDT2.pf", "ZDT3.pf","ZDT4.pf", "DTLZ1.2D.pf", "WFG2.2D.pf"} ;
		exp.indicatorList_ = new String[] { "HV", "SPREAD", "EPSILON" };

		int numberOfAlgorithms = exp.algorithmNameList_.length;

		exp.experimentBaseDirectory_ = "/Users/qrafzv/Documents/workspace/FPP/" + exp.experimentName_;
		exp.paretoFrontDirectory_ = "";

		exp.algorithmSettings_ = new Settings[numberOfAlgorithms];

		exp.independentRuns_ = 10;

		exp.initExperiment();

		// Run the experiments
		int numberOfThreads;
		exp.runExperiment(numberOfThreads = 8);

		exp.generateQualityIndicators();

		// Generate latex tables (comment this sentence is not desired)
		exp.generateLatexTables();

		// Configure the R scripts to be generated
		int rows;
		int columns;
		String prefix;
		String[] problems;

		boolean notch;
		rows = 1;
		columns = 1;
		prefix = new String("carleton.FPP");
		problems = new String[] { "FogPlanningProblem3010" };

		exp.generateRBoxplotScripts(rows, columns, problems, prefix, notch = false, exp);
		exp.generateRWilcoxonScripts(problems, prefix, exp);

		/*
		 * rows = 2 ; columns = 3 ; prefix = new String("Problems"); problems = new
		 * String[]{"ZDT1", "ZDT2","ZDT3", "ZDT4", "DTLZ1", "WFG2"} ;
		 * 
		 * boolean notch ; exp.generateRBoxplotScripts(rows, columns, problems, prefix,
		 * notch = true, exp) ; exp.generateRWilcoxonScripts(problems, prefix, exp) ;
		 * 
		 */
		// Applying Friedman test
		Friedman test = new Friedman(exp);
		test.executeTest("EPSILON");
		test.executeTest("HV");
		test.executeTest("SPREAD");
	} // main
} // NSGAIIStudy
