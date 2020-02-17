/*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Joe Osborn
 *******************************************************************************/
package org.eclipse.ice.modeling;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * This class is a standalone class that uses the OWLApi to generate an ontology
 * for the proposed Workflow steps of eclipse ice
 * 
 * @author Joe Osborn
 *
 */
public class OWLWorkflowSteps {

	/**
	 * A global ontology manager which is used for various
	 * ontology tasks
	 */
	private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/**
	 * A data factory from which all of the components of the ontology will
	 * be created from
	 */
	private static OWLDataFactory factory = manager.getOWLDataFactory();
	
	
	/**
	 * A global IRI to be used throughout the ontology creation
	 */
	private static IRI ontologyIRI = null;
	
	/**
	 * An ontology to be added to
	 */
	private static OWLOntology ontology = null;
	

	/**
	 * Main function which creates and builds the ontology. 
	 * 
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 * @throws FileNotFoundException 
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		// Create the IRI with a default eclipse address
		ontologyIRI = IRI.create("http://localhost.whatever");
		
		// Create the ontology at the IRI
		ontology = manager.createOntology(ontologyIRI);
		
		createCommand();
		createStep();
		createProcedure();
		System.out.println(ontology);
		manager.saveOntology(ontology, new TurtleDocumentFormat(), new FileOutputStream("/home/4jo/eclipse-workspace/commands.txt"));
	}

	/**
	 * This function creates the Command class in the ontology
	 */
	private static void createCommand() {
		// Create a Command class
		OWLClass Command = factory.getOWLClass(IRI.create(ontologyIRI + "#Command"));
		// Create the property that commands have of owning a status
		OWLObjectProperty hasStatus = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "#hasStatus"));
		
		// Create two data properties that Commands have
		OWLDataProperty commandString = factory.getOWLDataProperty(IRI.create(ontologyIRI+"#command"));
		OWLDataProperty statusMsg = factory.getOWLDataProperty(IRI.create(ontologyIRI+"#status"));
	
		// Construct the CommandStatus enum in the ontology
		// Create a class
		OWLClass commandStatus = factory.getOWLClass(IRI.create(ontologyIRI + "#CommandStatus"));
		// Add instances of the class
		OWLNamedIndividual commandStatus1 = factory.getOWLNamedIndividual(IRI.create("#CommandStatus:SUCCESS"));
		OWLNamedIndividual commandStatus2 = factory.getOWLNamedIndividual(IRI.create("#CommandStatus:FAILED"));
		OWLNamedIndividual commandStatus3 = factory.getOWLNamedIndividual(IRI.create("#CommandStatus:RUNNING"));
		// Create the axiom naming the instances as subclasses
		OWLAxiom commandStatusEnum = factory.getOWLSubClassOfAxiom(commandStatus, factory.getOWLObjectOneOf(commandStatus1,commandStatus2,commandStatus3));
		manager.addAxiom(ontology, commandStatusEnum);
		
		OWLDatatype stringDataType = factory.getStringOWLDatatype();
		
		Set<OWLAxiom> procedureRangeDomains = new HashSet<OWLAxiom>();
		
		procedureRangeDomains.add(factory.getOWLDataPropertyDomainAxiom(commandString, Command));
		procedureRangeDomains.add(factory.getOWLDataPropertyRangeAxiom(commandString, stringDataType));
		procedureRangeDomains.add(factory.getOWLDataPropertyDomainAxiom(statusMsg, Command));
		procedureRangeDomains.add(factory.getOWLObjectPropertyDomainAxiom(hasStatus, Command));
	
		ontology.add(procedureRangeDomains);
		
		// Commands can have at most one status property. So inform the ontology.
		OWLFunctionalDataPropertyAxiom statusMsgFuncAx = factory.getOWLFunctionalDataPropertyAxiom(statusMsg);
		manager.addAxiom(ontology, statusMsgFuncAx);
	}
	
	private static void createStep() {
		
	
	}
	
	/**
	 * This function creates several classes that are used in the ontology
	 * and adds them.
	 */
	@SuppressWarnings("static-access")
	private static void createProcedure() {
		// Create a class of the ontology, in this case the Procedure class
		OWLClass procedure = factory.getOWLClass(IRI.create(ontologyIRI + "#Procedure"));
		
		// Get the integer data type for adding the axioms to the ontology
		OWLDatatype integerDataType = factory.getIntegerOWLDatatype();
		
		// Create data properties for this class
		OWLDataProperty numSteps = factory.getOWLDataProperty(IRI.create(ontologyIRI+"#numSteps"));
		OWLDataProperty curIndex = factory.getOWLDataProperty(IRI.create(ontologyIRI+"#curIndex"));
		OWLDataProperty commands = factory.getOWLDataProperty(IRI.create(ontologyIRI+"#commands"));
		
		
		
		// Create a HashSet which will contain all of the procedure domains and
		// associated ranges of those domains
		Set<OWLAxiom> procedureDomains = new HashSet<OWLAxiom>();
		// Add the numSteps, curIndex property, as an integer
		procedureDomains.add(factory.getOWLDataPropertyDomainAxiom(numSteps, procedure));
		procedureDomains.add(factory.getOWLDataPropertyRangeAxiom(numSteps, integerDataType));
		procedureDomains.add(factory.getOWLDataPropertyDomainAxiom(curIndex, procedure));
		procedureDomains.add(factory.getOWLDataPropertyRangeAxiom(curIndex, integerDataType));
		
		
		
		ontology.add(procedureDomains);
	}
	
}
