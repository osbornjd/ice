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
package org.eclipse.ice.commands;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.common.SshConstants;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages remote connections, and as such interfaces with all
 * classes that are associated with remote connections.
 * 
 * @author Joe Osborn
 *
 */
public class ConnectionManager {
	private static final int DEFAULT_TIMEOUT = 10000;

	/**
	 * A HashMap of available Connections to the ConnectionManager, organized by the
	 * name of the connection and the connection itself.
	 * 
	 */
	private HashMap<String, Connection> connectionList = new HashMap<String, Connection>();

	/**
	 * Logger for handling event messages and other information.
	 */
	private final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

	/**
	 * A boolean that the user can set to disable ssh StrictHostKeyChecking. Set to
	 * true by default since this is the most secure way.
	 */
	private boolean requireStrictHostKeyChecking = true;

	/**
	 * This is a list of authorization types for JSch to allow authentication via.
	 * The default types added automatically are ssh-rsa and ecdsa-sha2-nistp256.
	 * Clients can add additional types should they need to.
	 */
	private ArrayList<String> authTypes = new ArrayList<String>();

	/**
	 * String containing the path to the known hosts directory. Can be set to
	 * something else if the user has a different default known_host
	 */
	private String knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";

	/**
	 * Default Constructor
	 */
	public ConnectionManager() {
		// If the OS is windows, then change the known hosts to be windows style
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			knownHosts = System.getProperty("user.home") + "\\.ssh\\known_hosts";
		}

		// Add the default authorization types
		authTypes.add("ssh-rsa");
		authTypes.add("ecdsa-sha2-nistp256");
	}

	/**
	 * Opens, and thus begins, a connection to a remote system. Also adds the
	 * connection to the list of connections given in
	 * {@link org.eclipse.ice.commands.ConnectionManager#connectionList}
	 * 
	 * @param config - ConnectionConfiguration to be used to open connection
	 * @return Connection - returns connection if successful, null otherwise
	 */
	public Connection openConnection(ConnectionConfiguration config) throws IOException {
		// The new connection to be opened
		Connection newConnection = new Connection(config);

		SshClient client = SshClient.setUpDefaultClient();
		client.start();

		newConnection.setClient(client);

		logger.info("Trying to open the connection");

		// Get the information necessary to open the connection
		if (newConnection.getConfiguration() != null) {
			ConnectionAuthorizationHandler auth = newConnection.getConfiguration().getAuthorization();
			String username = auth.getUsername();
			String hostname = auth.getHostname();

			// Try go get and open the new session
			try {
				ClientSession session = client.connect(username, hostname, SshConstants.DEFAULT_PORT)
						.verify(DEFAULT_TIMEOUT).getSession();
				newConnection.setSession(session);
			} catch (IOException e) {
				logger.error("Couldn't open session with given username and hostname. Exiting.", e);
				throw e;
			}

			// Set the delegate to reject all, then set the key verifier to check the strict
			// option
			ServerKeyVerifier delegate = RejectAllServerKeyVerifier.INSTANCE;
			if (knownHosts != null) {
				newConnection.getSession().setServerKeyVerifier(new DefaultKnownHostsServerKeyVerifier(delegate,
						requireStrictHostKeyChecking, Paths.get(knownHosts)));
			} else {
				newConnection.getSession().setServerKeyVerifier(
						new DefaultKnownHostsServerKeyVerifier(delegate, requireStrictHostKeyChecking));
			}

			// Connect the session
			try {
				// Authorize the JSch session with a ConnectionAuthorizationHandler
				authorizeSession(newConnection);
			} catch (IOException e) {
				logger.error("Couldn't connect to session with given username and/or password/key. Exiting.", e);
				throw e;
			}

			// Add the connection to the list since it was successfully created
			connectionList.put(newConnection.getConfiguration().getName(), newConnection);

			logger.info("Connection at " + username + "@" + hostname + " established successfully");

			// Upon success, return the opened connection
			return newConnection;
		}

		// If the connectionConfiguration was not properly specified, or an error
		// occurred, return null
		return null;
	}

	/**
	 * This function deals with the new connection authentication. It takes a
	 * connection that is being opened, and decides what kind of authentication to
	 * provide JSch depending on whether or not a password exists in the
	 * ConnectionAuthorizationHandler.
	 * 
	 * @param connection
	 * @throws JSchException
	 */
	private void authorizeSession(Connection connection) throws IOException {
		// Get the authorization information
		ConnectionAuthorizationHandler auth = connection.getConfiguration().getAuthorization();

		// If a password was set, then try to authenticate with the password
		if (auth.getPassword() != null) {
			logger.info("Trying to authenticate with a password");
			// Get the password first. If authorization is a text file, then
			// username and hostname will be set. Otherwise user must set them
			char[] pwd = auth.getPassword();

			// Pass it to the session
			connection.getSession().addPasswordIdentity(String.valueOf(pwd));

			// Erase contents of pwd and fill with null
			Arrays.fill(pwd, Character.MIN_VALUE);
			auth.setPassword(null);

			// Set the authentication requirements
			connection.getSession().setUserAuthFactoriesNames("publickey", "password");
		} else {
			logger.info("Trying to authenticate with a key");
			// Otherwise try a key authentication
			String keyPath = ((KeyPathConnectionAuthorizationHandler) auth).getKeyPath();
			if (keyPath != null) {
				FileKeyPairProvider provider = new FileKeyPairProvider() {
					@Override
					public String toString() {
						return FileKeyPairProvider.class.getSimpleName() + "[clientIdentitiesProvider]";
					}
				};
				provider.setPaths(Collections.singleton(Paths.get(keyPath)));
				connection.getClient().setKeyIdentityProvider(provider);
			}
		}

		connection.getSession().auth().verify(FactoryManager.DEFAULT_AUTH_TIMEOUT);
		return;
	}

	/**
	 * This function opens a forwarding connection between one already established
	 * connection to a remote system and an additional remote system specified by
	 * the ConnectionConfiguration. This allows ports to be opened up across
	 * multiple machines, as in: System A --> System B (intermediateConn) --> System
	 * C (config). So the connection that is returned from this function connects
	 * System A to System C.
	 * 
	 * @param intermediateConn - intermediate jump host
	 * @param config           - ConnectionConfiguration for final host destination
	 * @return - Connection if successfully established, null otherwise
	 * @throws IOException
	 */
	public Connection openForwardingConnection(Connection intermediateConn, ConnectionConfiguration config)
			throws IOException {

		// First check that the first connection actually is established
		if (!isConnectionOpen(intermediateConn.getConfiguration().getName())) {
			logger.error("Can't forward a connection with an unopened intermediate connection! Returning null.");
			return null;
		}

		// Make the forwarded connection
		Connection forwardConnection = new Connection(config);

		// Get the intermediate client and session
		SshClient intermClient = intermediateConn.getClient();
		ClientSession intermSesh = intermediateConn.getSession();

		// Check that the intermediate session is actually connected and open
		if (!intermSesh.isOpen()) {
			logger.error("Can't forward a connecion with an unopened intermediate session! Returning null.");
			return null;
		}

		// Set the new connection to have the same ssh client
		forwardConnection.setClient(intermClient);
		
		/**
		 * To make this work, we will set up a port forwarding from the local host to
		 * the ssh port of the final destination host through the originally opened
		 * session. Thus, the connection should go from the local host, to the (below)
		 * assignedPort, and then to port 22 on the final destination host.
		 */
		
		SshdSocketAddress forwardAdd = new SshdSocketAddress(config.getAuthorization().getHostname(), 22);
		SshdSocketAddress localAdd = new SshdSocketAddress("localhost",0);
		SshdSocketAddress address = intermSesh.startLocalPortForwarding(localAdd,forwardAdd);
		
		String username = config.getAuthorization().getUsername();
		String hostname = config.getAuthorization().getHostname();
		
		// Connect the session to access 127.0.0.1:address.getPort(), which is where the startLocalPortForwarding
		// is now matching the remote servers
		ClientSession forwardSession = forwardConnection.getClient().connect(username, "127.0.0.1", address.getPort()).verify(DEFAULT_TIMEOUT)
				.getSession();

		forwardConnection.setSession(forwardSession);

		// Authorize the session with password/key/etc.
		authorizeSession(forwardConnection);

		// Add the connection to the list since it was successfully created
		connectionList.put(forwardConnection.getConfiguration().getName(), forwardConnection);

		logger.info("Connection at " + username + "@" + hostname + " established successfully through "
				+ intermediateConn.getConfiguration().getAuthorization().getUsername() + "@" +
				  intermediateConn.getConfiguration().getAuthorization().getHostname());

		return forwardConnection;
	}

	/**
	 * This function finds the particular connection requested by name in the list
	 * of all connections and returns it.
	 * 
	 * @param connectionName - name of connection to search for
	 * @return - Connection instance which was requested
	 */
	public Connection getConnection(String connectionName) {
		// Find the hashmap instance, and return it
		Connection returnConnection = connectionList.get(connectionName);
		if (returnConnection == null) {
			logger.warn("Couldn't find an existing connection with the name " + connectionName + ".");
		}
		return returnConnection;
	}

	/**
	 * Closes a particular connection as specified
	 * 
	 * @param connectionName - name of connection to be closed
	 */
	public void closeConnection(String connectionName) {

		// Get the connection that was passed
		Connection connection = getConnection(connectionName);

		// Check the channels first
		if (connection.getExecChannel() != null) {
			if (connection.getExecChannel().isOpen()) {
				try {
					connection.getExecChannel().close();
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage());
				}
			}
		}
		if (connection.getSftpChannel() != null) {
			if (connection.getSftpChannel().isOpen()) {
				try {
					connection.getSftpChannel().close();
				} catch (IOException e) {
					logger.error(e.getLocalizedMessage());
				}
			}
		}
		// Disconnect the session. If the session was not connected in the first place,
		// it does nothing
		try {
			connection.getSession().close();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
		// Confirm with the logger
		logger.debug("Connection " + connectionName + "@"
				+ connection.getConfiguration().getAuthorization().getHostname() + " closed");

		return;
	}

	/**
	 * This function closes/disconnects and removes a particular connection from the
	 * manager's connection list
	 * 
	 * @param connectionName - name of connection to remove
	 */
	public void removeConnection(String connectionName) {
		// Close the connection first
		if (!isConnectionOpen(connectionName))
			closeConnection(connectionName);
		// Remove it from the list of connections
		connectionList.remove(connectionName);
	}

	/**
	 * This function removes all particular connections from the connection list
	 */
	public void removeAllConnections() {
		// First make sure all connections have been disconnected
		closeAllConnections();
		// Remove all of the items from the hashmap
		connectionList.clear();
	}

	/**
	 * Closes all connections that remain open.
	 */
	public void closeAllConnections() {
		// Iterate over all available connections in the list and disconnect
		for (Connection connection : connectionList.values()) {
			if (connection.getExecChannel() != null) {
				if (connection.getExecChannel().isOpen()) {
					try {
						connection.getExecChannel().close();
					} catch (IOException e) {
						logger.error(e.getLocalizedMessage());
					}
				}
			}
			if (connection.getSftpChannel() != null) {
				if (connection.getSftpChannel().isOpen()) {
					try {
						connection.getSftpChannel().close();
					} catch (IOException e) {
						logger.error(e.getLocalizedMessage());
					}
				}
			}
			try {
				connection.getSession().close();
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

	}

	/**
	 * This function allows adding a user defined connection to the connection
	 * manager rather than opening a default connection through the function
	 * {@link ConnectionManager#openConnection(ConnectionConfiguration)}.
	 * 
	 * @param connection
	 */
	public void addConnection(Connection connection) {
		connectionList.put(connection.getConfiguration().getName(), connection);
	}

	/**
	 * This function lists all the connections (and their statuses, i.e. if open or
	 * not) to the logger, if so desired. Useful for checking the connections and
	 * their statuses.
	 */
	public void listAllConnections() {
		// Iterate over all available connections
		for (String name : connectionList.keySet()) {
			// Build a message
			String msg = null;
			// Get the host for the connection
			String host = connectionList.get(name).getConfiguration().getAuthorization().getHostname();
			// Get the username for the connection
			String username = connectionList.get(name).getConfiguration().getAuthorization().getUsername();
			// Check the status. If it is open or closed (i.e. connected or disconnected)
			String status = "";
			if (isConnectionOpen(name))
				status = "open";
			else
				status = "closed";

			// Build a message to send to the logger
			msg = "Connection " + name + ": " + username + "@" + host + " is " + status;

			logger.info(msg);
		}

	}

	/**
	 * This function checks whether or not a particular connection is currently
	 * connected.
	 * 
	 * @param connectionName - name of connection to check
	 * @return - boolean indicating whether or not it is connected (true) or not
	 *         (false)
	 */
	public boolean isConnectionOpen(String connectionName) {
		Connection connection = getConnection(connectionName);
		return connection.getSession().isOpen();
	}

	/**
	 * Setter function for
	 * {@link org.eclipse.ice.commands.ConnectionManager#connectionList}
	 * 
	 * @param connections
	 */
	public void setConnectionList(HashMap<String, Connection> connectionList) {
		this.connectionList = connectionList;
	}

	/**
	 * Getter function for
	 * {@link org.eclipse.ice.commands.ConnectionManager#connectionList}
	 * 
	 * @return
	 */
	public HashMap<String, Connection> getConnectionList() {
		return connectionList;
	}

	/**
	 * Setter for whether or not connections should be open with the requirement of
	 * StrictHostKeyChecking
	 * 
	 * @param requireStrictHostKeyChecking
	 */
	public void setRequireStrictHostKeyChecking(boolean requireStrictHostKeyChecking) {
		this.requireStrictHostKeyChecking = requireStrictHostKeyChecking;
	}

	/**
	 * Setter for known host directory path
	 * {@link org.eclipse.ice.commands.ConnectionManager#knownHosts}
	 * 
	 * @param knownHosts
	 */
	public void setKnownHosts(String knownHosts) {
		this.knownHosts = knownHosts;
	}

	/**
	 * This function allows clients to add an authorization type for which JSch can
	 * authorize with. ssh-rsa and ecdsa-sha2-nistp256 are added by default.
	 * 
	 * @param type
	 */
	public void addAuthorizationType(String type) {
		authTypes.add(type);
	}
}
