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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.sshd.client.subsystem.sftp.SftpClient;
import org.eclipse.ice.commands.CommandStatus;
import org.eclipse.ice.commands.Connection;
import org.eclipse.ice.commands.ConnectionAuthorizationHandler;
import org.eclipse.ice.commands.ConnectionAuthorizationHandlerFactory;
import org.eclipse.ice.commands.ConnectionConfiguration;
import org.eclipse.ice.commands.ConnectionManager;
import org.eclipse.ice.commands.ConnectionManagerFactory;
import org.eclipse.ice.commands.HandleType;
import org.eclipse.ice.commands.RemoteMoveFileCommand;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for class {@link org.eclipse.ice.commands.RemoteMoveFileCommand}.
 * 
 * @author Joe Osborn
 *
 */
public class RemoteMoveFileCommandTest {

	/**
	 * A string for a source file for the remote move
	 */
	String source = null;

	/**
	 * A string for a destination for the remote move
	 */
	String dest = null;

	/**
	 * Make a RemoteFileHandlerTest to take advantage of much of the code which
	 * makes/deletes local/remote files
	 */
	RemoteFileHandlerTest handlerTest = new RemoteFileHandlerTest();

	/**
	 * This function sets up the dummy connection for file transferring
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Set up the connection using the code already established in
		// RemoteFileHandlerTest

		RemoteFileHandlerTest.setUpBeforeClass();

	}

	/**
	 * This function deletes all of the connections in the connection manager once
	 * the tests have run and completed.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionManager manager = ConnectionManagerFactory.getConnectionManager();
		manager.removeAllConnections();
	}

	/**
	 * This function tests moving a file through a forwarded jump host connection,
	 * where e.g. one is operating the Commands package on a host A and wants to
	 * move a file between remote host B and remote host C through a forwarded port.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoteJumpHostMoveFileCommand() throws Exception {
		// Get the connection manager for holding connection info
		ConnectionManager manager = ConnectionManagerFactory.getConnectionManager();
		
	// Need to create a remote source on other host
		// For now just use a file that is already there
		source = "/home/4jo/remoteCommandDirectory/someInputFile.txt";
		
		// Just make the destination the /tmp/ directory
		dest = "/tmp/";
	
		// First create the forwarding connection
		ConnectionAuthorizationHandlerFactory authFactory = new ConnectionAuthorizationHandlerFactory();
		// Request a ConnectionAuthorization of type text file which contains the
		// credentials
		String keyPath = System.getProperty("user.home") + "/.ssh/denisovankey";
		ConnectionAuthorizationHandler auth = authFactory.getConnectionAuthorizationHandler("keypath",
				keyPath);
		auth.setHostname("denisovan");
		auth.setUsername("4jo");
		ConnectionConfiguration config = new ConnectionConfiguration();
		config.setAuthorization(auth);
		config.setName("denisovanConnection");
		
		
		
		// Read in a dummy configuration file that contains credentials
		String credFile = "/tmp/ice-remote-creds.txt";
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			credFile = "C:\\Users\\Administrator\\ice-remote-creds.txt";
		ConnectionAuthorizationHandler intermauth = authFactory.getConnectionAuthorizationHandler("text",
				credFile);

		// Setup the configuration
		ConnectionConfiguration secondConn = new ConnectionConfiguration();
		secondConn.setAuthorization(intermauth);
		secondConn.setName("executeConnection");
		secondConn.deleteWorkingDirectory(false);
		
		
		
		//Connection forwardConnection = manager.openForwardingConnection(otherConn, dummyhostConfig);

		// Ensure forwarded connection was properly opened
		//assertTrue(manager.isConnectionOpen(forwardConnection.getConfiguration().getName()));
		
		System.out.println("\n\n\n\n\n Forwarded Connection is opened");
		// Configure the command to move the file
		RemoteMoveFileCommand command = new RemoteMoveFileCommand();
		command.setConnectionConfiguration(config);
		command.setForwardConnection(manager.openConnection(secondConn));
		command.openAndSetConnection();
		command.setMoveType(HandleType.remoteRemoteJumpHost);
		command.setConfiguration(source, dest);
		CommandStatus status = command.execute();
		
		// Check that the execution returned a success
		assertEquals(CommandStatus.SUCCESS, status);
		
		String filename = source.substring(source.lastIndexOf("/") + 1);
		// Also check that the file actually exists
		//assertTrue(remotePathExists(forwardConnection, dest + filename));
		
		// Clean up
		//handlerTest.deleteRemoteSource();
		// Delete the moved file
		//SftpClient sftpChannel = forwardConnection.getSftpChannel();
		//sftpChannel.remove(dest + filename);
		
	}

	/**
	 * Test for moving a file only on the remote system
	 */
	@Test
	public void testRemoteMoveFileCommand() throws Exception {

		handlerTest.createRemoteSource();
		handlerTest.createRemoteDestination();
		source = handlerTest.getSource();
		dest = handlerTest.getDestination();

		RemoteMoveFileCommand command = new RemoteMoveFileCommand();
		// These functions are nominally handled by the FileHandler. But, when testing
		// this class alone, we need to set them individually
		command.setMoveType(HandleType.remoteRemote);
		command.setConnection(handlerTest.getConnection());
		command.setConfiguration(source, dest);
		CommandStatus status = command.execute();

		// Assert that the command status was properly configured
		assertEquals(CommandStatus.SUCCESS, status);

		// Assert that the command was actually successful and that command status
		// wasn't inadvertently set to successful
		assertTrue(remotePathExists(handlerTest.getConnection(), dest));

		// Delete the temporary files that were created to test
		// Don't need to delete the source since it was moved
		handlerTest.deleteRemoteDestination();

	}

	/**
	 * Test for method {@link org.eclipse.ice.commands.RemoteMoveFileCommand()}
	 * where the file is downloaded from the remote host
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoteMoveFileCommandDownload() throws Exception {
		// Create a remote source to download
		handlerTest.createRemoteSource();
		source = handlerTest.getSource();

		// Create a local destination to put that source
		handlerTest.createLocalDestination();
		dest = handlerTest.getDestination();

		RemoteMoveFileCommand command = new RemoteMoveFileCommand();
		// These functions are nominally handled by the FileHandler. But, when testing
		// this class alone, we need to set them individually
		command.setMoveType(HandleType.remoteLocal);
		command.setConnection(handlerTest.getConnection());
		command.setConfiguration(source, dest);
		CommandStatus status = command.execute();

		// Assert that the command status was properly configured
		assert (status == CommandStatus.SUCCESS);

		// Assert that the command was actually successful and that command status
		// wasn't inadvertently set to successful
		assertTrue(localPathExists());

		// Delete the temporary files that were created to test
		handlerTest.deleteRemoteSource();
		handlerTest.deleteLocalDestination();

	}

	/**
	 * Test for method {@link org.eclipse.ice.commands.RemoteMoveFileCommand()}
	 * where the file is uploaded to the remote host
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoteMoveFileCommandUpload() throws Exception {
		// Create a local source file to move
		handlerTest.createLocalSource();
		source = handlerTest.getSource();
		// Create a remote destination to move it to
		handlerTest.createRemoteDestination();
		dest = handlerTest.getDestination();

		// Create the move command
		RemoteMoveFileCommand command = new RemoteMoveFileCommand();
		// These functions are nominally handled by the FileHandler. But, when testing
		// this class alone, we need to set them individually
		command.setMoveType(HandleType.localRemote);
		command.setConnection(handlerTest.getConnection());
		command.setConfiguration(source, dest);
		CommandStatus status = command.execute();

		// Assert that the command status was properly configured
		assertEquals(CommandStatus.SUCCESS, status);

		// Assert that the command was actually successful and that command status
		// wasn't inadvertently set to successful
		assertTrue(remotePathExists(handlerTest.getConnection(), dest));

		// Delete the temporary files that were created to test
		handlerTest.deleteLocalSource();
		handlerTest.deleteRemoteDestination();

	}

	/**
	 * This function checks if a remote file exists on the host
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean remotePathExists(Connection connection, String file) throws Exception {

		// Connect the channel from the connection
		SftpClient sftpChannel = connection.getSftpChannel();

		try {
			sftpChannel.lstat(file);
		} catch (IOException e) {
			// If an exception is caught, this means the file was not there.
			return false;
		}
		// So if we get here, then the file exists
		return true;
	}

	/**
	 * This function checks if a local path exists on the host
	 * 
	 * @return
	 */
	public boolean localPathExists() {
		Path path = Paths.get(dest);
		return Files.exists(path);
	}

}
