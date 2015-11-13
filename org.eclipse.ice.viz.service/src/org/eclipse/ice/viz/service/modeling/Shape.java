/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.modeling;

/**
 * A controller for a shape part, which exposes ShapeComponent functionality.
 * 
 * @author Robert Smith
 *
 */
public class Shape extends AbstractController {

	/**
	 * THe nullary constructor
	 */
	public Shape() {
		super();
	}

	/**
	 * The default constructor.
	 * 
	 * @param model
	 *            The controller's model
	 * @param view
	 *            The controller's view
	 */
	public Shape(ShapeComponent model, AbstractView view) {
		super(model, view);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.modeling.AbstractController#removeEntity(org.
	 * eclipse.ice.viz.service.modeling.AbstractController)
	 */
	@Override
	public void removeEntity(AbstractController entity) {
		super.removeEntity(entity);

		// If this was the entity's parent, remove it
		if (entity instanceof Shape
				&& entity.getEntitiesByCategory("Parent").contains(this)) {
			entity.removeEntity(this);
		}
	}

	/**
	 * Set the shape's parent shape. Shapes can have at most one parent, and
	 * this operation will remove any existing parent.
	 * 
	 * @param parent
	 *            The new shape which serves as this shape's parent.
	 */
	public void setParent(Shape parent) {
		((ShapeComponent) model).setParent(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.modeling.AbstractController#clone()
	 */
	@Override
	public Object clone() {

		// Create a new shape from clones of the model and view
		Shape clone = new Shape();

		// Copy any other data into the clone
		clone.copy(this);

		return clone;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ice.viz.service.modeling.AbstractController#copy(org.eclipse.
	 * ice.viz.service.modeling.AbstractController)
	 */
	@Override
	public void copy(AbstractController source) {

		// Create the model and give it a reference to this
		model = new ShapeComponent();
		model.setController(this);

		// Copy the other object's data members
		model.copy(source.model);
		view = (AbstractView) source.view.clone();

		// Register as a listener to the model and view
		model.register(this);
		view.register(this);
	}
}
