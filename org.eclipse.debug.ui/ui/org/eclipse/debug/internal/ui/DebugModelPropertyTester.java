/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;

/**
 * This class is used to check properties of a debug model.  Two properties can be checked.
 * Using "getModelIdentifier" compares the debug model identifier of the receiver against the
 * expected value passed as an argument.  The "isTerminatedOrDisconnected" property checks if
 * the receiver is terminated or disconnected.
 *
 *	@since 3.3
 */
public class DebugModelPropertyTester extends PropertyTester {

	public static final String MODEL_TYPE_PROPERTY = "getModelIdentifier"; //$NON-NLS-1$
	public static final String IS_TERMINATED_OR_DISCONNECTED_PROPERTY = "isTerminatedOrDisconnected"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.PropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (MODEL_TYPE_PROPERTY.equals(property)){
			IDebugTarget target = null;
			if(receiver instanceof IProcess) {
				target = (IDebugTarget) ((IProcess)receiver).getAdapter(IDebugTarget.class);
			}
			else if(receiver instanceof IDebugElement) { 
				target = (IDebugTarget) ((IDebugElement)receiver).getAdapter(IDebugTarget.class);
			}
			if(target != null) {
				// check that the expected value argument is valid
				if (expectedValue == null || expectedValue.equals(IInternalDebugCoreConstants.EMPTY_STRING)){
					return false;
				}
				//!target.isTerminated() && !target.isDisconnected() 
				if(expectedValue.equals(target.getModelIdentifier())) {
					return true;
				}
			}
			return false;
		} else if (IS_TERMINATED_OR_DISCONNECTED_PROPERTY.equals(property)){
			if (receiver instanceof ITerminate && ((ITerminate)receiver).isTerminated()){
				return true;
			} if (receiver instanceof IDisconnect && ((IDisconnect)receiver).isDisconnected()){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}