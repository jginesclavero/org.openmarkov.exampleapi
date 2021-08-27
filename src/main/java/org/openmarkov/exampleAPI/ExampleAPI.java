package org.openmarkov.exampleAPI;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NodeNotFoundException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
//import org.openmarkov.gui.window.MainGUI;
import org.openmarkov.inference.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import py4j.GatewayServer;

/** The repository org.openmarkov.exampleAPI, stored in 
 * https://bitbucket.org/cisiad/org.openmarkov.exampleapi,
 * shows by means of an example how to use OpenMarkov as an API for
 * opening a probabilistic model, introducing evidence and showing the results
 * of inference. In this example the model is a Bayesian network for
 * the differential diagnosis of two diseases, A and B, based on a set
 * of findings, called "two-diseases.pgmx", that was created using 
 * OpenMarkov's GUI. It is stored in this repository at scr/main/resources.
 * It is also available at http://www.cisiad.uned.es/ProbModelXML/examples.
 * 
 * The ultimate goal of this example is to deploy the file exampleAPI.jar
 * on a nexus server using Maven. By executing the jar file, the user can
 * observe the posterior probability of each disease given two different
 * sets of findings.
 * 
 *
 */
public class ExampleAPI {
	public static ProbNetInfo probNetInfo;
	public static ProbNet probNet;
	public static List<Variable> variablesOfInterest;
	public static EvidenceCase evidence;
	//public static MainGUI openMarkovGUI;
	public static void main(String[] args) {
		//openMarkovGUI = new MainGUI();
		//openMarkovGUI.setVisible(true);
		GatewayServer gatewayServer = new GatewayServer(new ExampleAPI());
        gatewayServer.start();
        System.out.println("Gateway Server Started");
	}

	// Constructor
	public ExampleAPI() {
		evidence = new EvidenceCase();
		// Create an evidence case
//		// (An evidence case is composed of a set of findings)
//		EvidenceCase evidence = new EvidenceCase();
//
//		// The first finding we introduce is the presence
//		// of the symptom
//		evidence.addFinding(probNet, "Symptom", "present");
//
//		// Create an instance of the inference algorithm
//		// In this example, we use the variable elimination algorithm
//		//InferenceAlgorithm variableElimination = new VariableElimination(probNet);
//
//
//		// Add the evidence to the algorithm
//		// The resolution of the network consists of finding the
//		// optimal policies.
//		// In the case of a model that does not contain decision nodes
//		// (for example, a Bayesian network), there is no difference between
//		// pre-resolution and post-resolution evidence, but if the model
//		// contained decision nodes (for example, an influence diagram)
//		// evidence introduced before resolving the network is treated
//		// differently from that introduce afterwards.
//		//variableElimination.setPreResolutionEvidence(evidence);
//
//		// We are interested in the posterior probabilities of the diseases
//		Variable disease1 = probNet.getVariable("Disease 1");
//		Variable disease2 = probNet.getVariable("Disease 2");
//		List<Variable> variablesOfInterest = Arrays.asList(disease1, disease2);
//
//		Propagation propagation = new VEPropagation(probNet,variablesOfInterest,evidence,new EvidenceCase(),null);
//
//
//		// Compute the posterior probabilities
//		//Map<Variable, TablePotential> posteriorProbabilities =
//		//		variableElimination.getProbsAndUtilities();
//
//		Map<Variable, TablePotential> posteriorProbabilities =
//				propagation.getPosteriorValues();
//
//		// Print the posterior probabilities on the standard output
//		printResults(evidence, variablesOfInterest, posteriorProbabilities);
//
//		// Add a new finding and do inference again
//		// (We see that the presence of the sign confirms the presence
//		// of Disease 1 with high probability and explains away Disease 2)
//		evidence.addFinding(probNet, "Sign", "present");
//
//		propagation = new VEPropagation(probNet,variablesOfInterest,evidence,new EvidenceCase(),null);
//
//		//posteriorProbabilities = variableElimination.getProbsAndUtilities(variablesOfInterest);
//		posteriorProbabilities = propagation.getPosteriorValues();
//
//
//		printResults(evidence, variablesOfInterest, posteriorProbabilities);
		
	}
	
	public void initNet(String bayesNetworkName, String path) throws FileNotFoundException, ParserException, NotEvaluableNetworkException {
		InputStream file = new FileInputStream(path + bayesNetworkName);
		// Load the Bayesian network
		PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
		probNetInfo = pgmxReader.loadProbNetInfo( bayesNetworkName, file );
		probNet = probNetInfo.getProbNet();
        if ( probNet == null ) {
            throw new ParserException( "No ProbNet in ProbNetInfo." );
        }
        LogManager.getLogger(getClass()).debug("Network opened");
        variablesOfInterest = probNet.getVariables();
	}
	
	public ProbNet getProbNet(){
		return probNet;
	}
	
	public List<EvidenceCase> getEvidence(){
		return probNetInfo.getEvidence();
	}
	
	public HashMap<Variable, TablePotential> setFinding(String variable, String state) throws NotEvaluableNetworkException, IncompatibleEvidenceException{
		VEPropagation vePropagation = new VEPropagation(probNet);
		try {
			vePropagation.setVariablesOfInterest(variablesOfInterest);
			vePropagation.setPreResolutionEvidence(evidence);
			evidence.addFinding(probNet, variable, state);
			vePropagation.setPostResolutionEvidence(evidence);
		} catch (NodeNotFoundException | InvalidStateException | IncompatibleEvidenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return vePropagation.getPosteriorValues();
	}
	
	public HashMap<Variable, TablePotential> getPropagation(List<EvidenceCase> evidenceList) throws NotEvaluableNetworkException, IncompatibleEvidenceException{
		VEPropagation vePropagation = new VEPropagation(probNet);
		vePropagation.setVariablesOfInterest(variablesOfInterest);
		vePropagation.setPreResolutionEvidence(evidence);
		return vePropagation.getPosteriorValues();
	}
	

	/** Print the posterior probabilities of the variables of interest on the standard output
	 * @param evidence <code>EvidenceCase</code> The set of observed findings
	 * @param variablesOfInterest <code>List</code> of <code>Variable</code> The variables
	 *  whose posterior probability we are interested in
	 * @param posteriorProbabilities <code>HashMap</code>. Each <code>Variable</code>
	 * is mapped onto a <code>TablePotential</code> containing its posterior probability */
	public void printResults(EvidenceCase evidence, List<Variable> variablesOfInterest, 
			Map<Variable, TablePotential> posteriorProbabilities) {
		// Print the findings
		System.out.println("Evidence:");
		for (Finding finding : evidence.getFindings()) {
			System.out.print("  " + finding.getVariable() + ": ");
			System.out.println(finding.getState());
		}
		// Print the posterior probability of the state "present" of each variable of interest
		System.out.println("Posterior probabilities: ");
		for (Variable variable : variablesOfInterest) {
			double value;
			TablePotential posteriorProbabilitiesPotential = posteriorProbabilities.get(variable);
			System.out.print("  " + variable + ": ");
			int stateIndex;
			try {
				stateIndex = variable.getStateIndex("present");
				value = posteriorProbabilitiesPotential.values[stateIndex];
				System.out.println(Util.roundedString(value, "0.001"));
			} catch (InvalidStateException e) {
				System.err.println("State \"present\" not found for variable \"" 
						   + variable.getName() + "\".");
				e.printStackTrace();
			}
		}
		System.out.println();
	}

}