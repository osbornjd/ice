/*******************************************************************************
 * Copyright (c) 2015- UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - 
 *   Jordan Deyton
 *******************************************************************************/
package org.eclipse.ice.viz.service;

import org.eclipse.ice.client.widgets.viz.service.IVizService;
import org.eclipse.ice.viz.service.preferences.CustomScopedPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This is an abstract base class for creating new {@link IVizService}s and
 * includes helpful methods that are frequently used.
 * 
 * @author Jordan Deyton
 *
 */
public abstract class AbstractVizService implements IVizService {

	/**
	 * A reference to the associated preference page's {@link IPreferenceStore}.
	 * If this has been determined previously, then it should be returned in
	 * {@link #getPreferenceStore()}.
	 */
	private IPreferenceStore preferenceStore = null;

	/**
	 * Gets the {@link IPreferenceStore} for the associated preference page.
	 * 
	 * @return The {@code IPreferenceStore} whose defaults should be set.
	 */
	protected IPreferenceStore getPreferenceStore() {
		if (preferenceStore == null) {
			// Get the PreferenceStore for the bundle.
			preferenceStore = new CustomScopedPreferenceStore(getClass());
		}
		return preferenceStore;
	}

}
