/*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jay Jay Billings, Joe Osborn
 *******************************************************************************/
package org.eclipse.ice.tests.commands;

import org.eclipse.ice.commands.CommandConfiguration;
import org.eclipse.ice.commands.CommandStatus;
import org.eclipse.ice.commands.ConnectionConfiguration;
import org.eclipse.ice.commands.LocalCommand;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for class {@link org.eclipse.ice.commands.LocalCommand}.
 * 
 * @author Joe Osborn
 *
 */
public class LocalCommandTest {

	/**
	 * Create some strings that correspond to an actual test script
	 */
	String executable = "./someExecutable.sh ${installDir}";
	String inputFile = "someInputFile.txt";
	String errorFile = "someErrFile.txt";
	String outputFile = "someOutFile.txt";
	String procs = "1";
	String installDir = "~/install";
	String os = "osx";
	String workingDirectory = "/";

	/**
	 * Put these in a command configuration instance to use
	 */
	CommandConfiguration commandConfig = new CommandConfiguration(3, executable, inputFile, errorFile, outputFile,
			procs, installDir, os, workingDirectory, true);

	/**
	 * The connection used (in this case, it is a local connection)
	 */
	ConnectionConfiguration connection;

	/**
	 * Set up by establishing that the test connection is local
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		// Use the function already defined in the command factory to get the
		// local host name
		CommandFactoryTest factory = new CommandFactoryTest();
		String hostname = factory.getLocalHostname();

		connection = new ConnectionConfiguration(hostname);

	}

	/**
	 * Test for method {@link org.eclipse.ice.commands.LocalCommand()} Tests check
	 * for proper configuration and checking of the LocalCommand member variables so
	 * that the Command can actually be run.
	 */
	@Test
	public void testLocalCommand() {

		System.out.println("Starting testLocalCommand");

		// Now make a "real" command configuration to test

		LocalCommand realCommand = new LocalCommand(connection, commandConfig);
		CommandStatus testStatus = realCommand.getStatus();

		assert (testStatus == CommandStatus.PROCESSING);
		System.out.println("Finished testConfiguration\n");

	}

	/**
	 * Test method for {@link org.eclipse.ice.commands.LocalCommand#Execute()}. This
	 * test should "fail" by default since the CommandConfiguration
	 * executableDictionary does not point to a real executable. This is a test of
	 * the API catching an incorrectly configured command. For a test of a fully
	 * functional command, see {@link org.eclipse.ice.commands.testCommandFactory()}
	 */
	@Test
	public void testExecute() {
		System.out.println("Starting testExecute with a non-existant executable.\n");

		CommandConfiguration badConfig = new CommandConfiguration(2, "fake_exec.sh", "inputfile", "errfile.txt",
				"outfile.txt", "1", "installDir", "osx", "somedirectory", true);

		LocalCommand testCommand = new LocalCommand(connection, badConfig);

		CommandStatus testStatus = testCommand.execute();

		assert (testStatus == CommandStatus.FAILED);
		System.out.println("Example incorrect executable status should be FAILED and is: " + testStatus);
		System.out.println("Finished testExecute\n");
	}

}
