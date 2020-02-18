/**
 * /*******************************************************************************
 * Copyright (c) 2019- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Joe Osborn
 *******************************************************************************/

package org.eclipse.ice.commands;

/**
 * An enum to determine what kind of move this is 
 * localRemote - local source, remote destination 
 * remoteLocal - remote source, local destination
 * remoteRemote - remote source, remote destination on same host
 * remoteRemoteJumpHost - remote source, remote destination on different remote hosts
 * 
 * @author Joe Osborn
 *
 */

public enum HandleType {
	localRemote, remoteLocal, remoteRemote, remoteRemoteJumpHost;
}