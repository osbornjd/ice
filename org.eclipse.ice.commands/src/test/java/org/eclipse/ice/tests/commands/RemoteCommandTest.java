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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.ice.commands.CommandConfiguration;
import org.eclipse.ice.commands.CommandStatus;
import org.eclipse.ice.commands.ConnectionAuthorizationHandler;
import org.eclipse.ice.commands.ConnectionAuthorizationHandlerFactory;
import org.eclipse.ice.commands.ConnectionConfiguration;
import org.eclipse.ice.commands.ConnectionManager;
import org.eclipse.ice.commands.ConnectionManagerFactory;
import org.eclipse.ice.commands.RemoteCommand;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for class {@link org.eclipse.ice.commands.RemoteCommand}.
 * 
 * @author Joe Osborn
 *
 */
public class RemoteCommandTest {

	/**
	 * A command configuration instance for testing
	 */
	CommandConfiguration commandConfig;

	/**
	 * A connect configuration instance for testing
	 */
	ConnectionConfiguration connectConfig = new ConnectionConfiguration();

	/**
	 * This function sets up the command and connection information to hand to the
	 * command
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		// Get the present working directory
		String pwd = System.getProperty("user.dir");

		// Add the following directories where the tests live
		pwd += "/src/test/java/org/eclipse/ice/tests/commands/";

		commandConfig = new CommandConfiguration();

		// Set the command to configure to a dummy hello world command
		// See {@link org.eclipse.ice.commands.CommandConfiguration} for detailed info
		// on each
		commandConfig.setCommandId(0);
		commandConfig.setExecutable("./test_code_execution.sh");
		commandConfig.addInputFile("someInputFile", "someInputFile.txt");
		commandConfig.setErrFileName("someErrFile.txt");
		commandConfig.setOutFileName("someOutFile.txt");
		commandConfig.setInstallDirectory("");
		commandConfig.setWorkingDirectory(pwd);
		commandConfig.setAppendInput(true);
		commandConfig.setNumProcs("1");
		commandConfig.setOS(System.getProperty("os.name"));
		commandConfig.setRemoteWorkingDirectory("/tmp/remoteCommandTestDirectory");

		// Set the connection configuration to a dummy remote connection
		// Make the connection configuration
		// Get a factory which determines the type of authorization
		ConnectionAuthorizationHandlerFactory authFactory = new ConnectionAuthorizationHandlerFactory();
		// Request a ConnectionAuthorization of type text file which contains the
		// credentials
		ConnectionAuthorizationHandler auth = authFactory.getConnectionAuthorizationHandler("text",
				"/tmp/ice-remote-creds.txt");
		// Set it
		connectConfig.setAuthorization(auth);
		connectConfig.setName("dummyConnection");
		connectConfig.setDeleteWorkingDirectory(true);
	}

	/**
	 * Run after the tests have finished processing. This function just removes the
	 * dummy text files that are created with log/error information from running
	 * various commands tests.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@AfterClass
	public static void tearDownAfterClass() throws IOException, InterruptedException {

		// Make and execute a simple command to remove the text files created
		// in these tests.

		// Make a string of all the output file names in this test
		String rm = "someOutFile.txt someErrFile.txt";
		ArrayList<String> command = new ArrayList<String>();
		// Build a command
		// TODO build this command for use in windows
		command.add("/bin/bash");
		command.add("-c");
		command.add("rm " + rm);
		// Execute the command with the process builder api
		ProcessBuilder builder = new ProcessBuilder(command);
		// Files exist in the top most directory of the package
		String topDir = System.getProperty("user.dir");
		File file = new File(topDir);
		builder.directory(file);
		// Process it
		Process job = builder.start();
		job.waitFor(); // wait for it to finish

		// Remove all connections that may remain from the manager
		ConnectionManager manager = ConnectionManagerFactory.getConnectionManager();
		manager.removeAllConnections();

	}

	/**
	 * Test for method {@link org.eclipse.ice.commands.RemoteCommand()}
	 */
	@Test
	public void testRemoteCommand() {
		System.out.println("Testing remote command configuration");

		// Get a command which just sets everything up
		RemoteCommand command = new RemoteCommand(commandConfig, connectConfig, null);

		// Get the status
		CommandStatus status = command.getStatus();

		// Check that the return is processing, i.e. we are ready to execute
		assert (status == CommandStatus.PROCESSING);

		System.out.println("Finished remote command configuration test.");
	}

	/**
	 * This tests that the job status is set to failed if an incorrect connection is
	 * established. Expect an exception since the connection will not be able to be
	 * established.
	 */
	@Test(expected = NullPointerException.class)
	public void testFailedConnectionRemoteCommand() {
		System.out.println("Testing remote command with a bad connection");

		// Make up some bad connection to test
		ConnectionConfiguration cfg = new ConnectionConfiguration();
		// Make the connection configuration
		// Get a factory which determines the type of authorization
		ConnectionAuthorizationHandlerFactory authFactory = new ConnectionAuthorizationHandlerFactory();
		// Request a ConnectionAuthorization of type text file which contains the
		// credentials
		ConnectionAuthorizationHandler auth = authFactory.getConnectionAuthorizationHandler("text",
				"/non/existent/path/creds.txt");
		// Set it
		cfg.setAuthorization(auth);
		// Make a command with a bad connection
		RemoteCommand command = new RemoteCommand(commandConfig, connectConfig, null);

		// Check that the command gives an error in its status due to poor connection
		assert (command.getStatus() == CommandStatus.INFOERROR);
	}

	/**
	 * Test method for executing remote command
	 * {@link org.eclipse.ice.commands.RemoteCommand#execute()}
	 */
	@Test
	public void testExecute() {
		System.out.println("\n\n\nTest remote command execute");

		// Make a command and execute the command
		RemoteCommand command = new RemoteCommand(commandConfig, connectConfig, null);
		CommandStatus status = command.execute();

		// Check that the command was successfully completed
		assert (status == CommandStatus.SUCCESS);

		System.out.println("Finished testing remote command execute");
	}

}
